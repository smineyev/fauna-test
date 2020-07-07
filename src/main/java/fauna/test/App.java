package fauna.test;

import com.faunadb.client.FaunaClient;
import com.faunadb.client.query.Expr;
import com.faunadb.client.types.Value;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.faunadb.client.query.Language.*;
import static fauna.test.Utils.timeit;
import static fauna.test.Utils.unchecked;

class App {
    static String DB_NAME = "demo";
    static String POSTS_COLLECTION = "posts";
    static String POSTS_INDEX = "posts_index";
    static String POSTS_BY_TITLE_INDEX = "posts_by_title_index";
    static int N = 100;

    static FaunaClient adminClient;
    static FaunaClient client;

    static Random rnd = new Random();

    static void init() throws Exception {
//        adminClient = FaunaClient.builder()
//                .withSecret("fnADsqnRZ2ACATRbbZj3XmVttEmlFAYH6h22G2Vp")
//                .build();
        adminClient = FaunaClient.builder()
                .withSecret("secret")
                .withEndpoint("http://localhost:8443")
                .build();

        createDb();
        createCollectionAndIndex();
    }

    private static void createCollectionAndIndex() throws Exception {
        Value collectionResults = client.query(
                CreateCollection(
                        Obj("name", Value(POSTS_COLLECTION))
                )
        ).get();
        System.out.println("Create Collection for " + DB_NAME + ":\n " + collectionResults + "\n");

        Value allIndexResults = client.query(
                CreateIndex(
                        Obj("name", Value(POSTS_INDEX), "source", Collection(Value(POSTS_COLLECTION)))
                )
        ).get();
        System.out.println("Create Index for " + DB_NAME + ":\n " + allIndexResults + "\n");

        Value indexByTitleResults = client.query(
                CreateIndex(
                        Obj("name", Value(POSTS_BY_TITLE_INDEX),
                            "source", Collection(Value(POSTS_COLLECTION)),
                            "terms", Arr(
                                    Obj("field",
                                            Arr(Value("data"), Value("title")))))
                )
        ).get();
        System.out.println("Create Index for " + DB_NAME + ":\n " + indexByTitleResults + "\n");
    }

    private static void createDb() throws InterruptedException, java.util.concurrent.ExecutionException {
        var dbResults = adminClient.query(
                Do(
                        If(
                                Exists(Database(DB_NAME)),
                                Delete(Database(DB_NAME)),
                                Value(true)
                        ),
                        CreateDatabase(Obj("name", Value(DB_NAME)))
                )
        ).get();
        System.out.println("Successfully created database: " + dbResults.at("name").to(String.class).get() + "\n" + dbResults + "\n");

        Value keyResults = adminClient.query(
                CreateKey(Obj("database", Database(Value(DB_NAME)), "role", Value("server")))
        ).get();

        String key = keyResults.at("secret").to(String.class).get();
        client = adminClient.newSessionClient(key);
        System.out.println("Connected to Fauna database " + DB_NAME + " with server role\n");
    }

    static void perfTest() throws Exception {
        timeit(""+N+" docs added",
                () -> IntStream.range(0, N).forEach(
                        i -> unchecked(() -> createPosts(i, 1))
                ));
        timeit("doc batch added",
                () -> {
                    createPosts(N+1, N);
                });
        timeit("all docs fetched",
                () -> {
                    var queryIndexResults = client.query(
                            SelectAll(Path("data", "id"),
                                    Paginate(
                                            Match(Index(Value(POSTS_INDEX)))
                                    ))
                    ).get();
                    System.out.println(queryIndexResults);
                });

        timeit("some docs fetched",
                () -> {
                    var queryIndexResults = client.query(
                                    Get(
                                        Match(Index(Value(POSTS_BY_TITLE_INDEX)), Value("Post #1"))
                                    )
                    ).get();
                    System.out.println(queryIndexResults);
                });
    }

    static private void createPosts(int startFrom, int num) throws Exception {
        Value res = client.query(
                Do (
                        exprCreatePost(startFrom, num)
                )
        ).get();
        System.out.println("Added "+num+" to " + POSTS_COLLECTION + ":\n " + res);
    }

    private static List<Expr> exprCreatePost(int startFrom, int num) {
        return IntStream.range(startFrom, startFrom + num)
                        .mapToObj(i ->
                                    Create(
                                            Collection(Value(POSTS_COLLECTION)),
                                            Obj("data",
                                                    Obj("title", Value("Post #" + i),
                                                            "cost", Value(rnd.nextInt(N)))
                                            )))
                        .collect(Collectors.toList());
    }

    public static void main(String[] args) throws Exception {
        init();
        perfTest();
    }
}

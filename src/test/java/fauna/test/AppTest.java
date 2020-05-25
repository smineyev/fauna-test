package fauna.test;

import com.faunadb.client.types.Value;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.faunadb.client.FaunaClient;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.faunadb.client.query.Language.*;
import static fauna.test.Utils.*;

class AppTest {
    static String DB_NAME = "demo";
    static String POSTS_COLLECTION = "posts";
    static String POSTS_INDEX = "posts_index";
    static int N = 10;

    static FaunaClient adminClient;
    static FaunaClient client;

    Random rnd = new Random();

    @BeforeAll static void init() throws Exception {
        adminClient = FaunaClient.builder()
                             .withSecret("fnADsqnRZ2ACATRbbZj3XmVttEmlFAYH6h22G2Vp")
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

        Value indexResults = client.query(
                CreateIndex(
                        Obj("name", Value(POSTS_INDEX), "source", Collection(Value(POSTS_COLLECTION)))
                )
        ).get();
        System.out.println("Create Index for " + DB_NAME + ":\n " + indexResults + "\n");
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

    @Disabled
    @Test void perfTest() throws Exception {
        timeit(""+N+" docs added",
                () -> IntStream.range(0, N).forEach(
                        i -> unchecked(() -> createPost(i))
                ));
        timeit("doc added",
                () -> {
                    createPost(N+1);
                });
        timeit("doc fetched",
                () -> {
                    var queryIndexResults = client.query(
                            SelectAll(Path("data", "id"),
                                    Paginate(
                                            Match(Index(Value(POSTS_INDEX)))
                                    ))
                    ).get();
                    System.out.println(queryIndexResults);
                });
    }

    private void createPost(int i) throws Exception {
        Value addFireResults = client.query(
                Create(
                        Collection(Value(POSTS_COLLECTION)),
                        Obj("data",
                                Obj("title", Value("Post #"+i),
                                    "cost", Value(rnd.nextInt(N)))
                        )
                )
        ).get();
        System.out.println("Added spell to collection " + POSTS_COLLECTION + ":\n " + addFireResults + "\n");
    }
}

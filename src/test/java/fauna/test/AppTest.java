package fauna.test;

import com.anaplan.dd.dsl.util.D3FileUtils;
import com.google.common.reflect.ClassPath;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class AppTest {
    @Test
    void test() throws IOException {
        var cp = ClassPath.from(D3FileUtils.class.getClassLoader());
        cp.getResources()
                .stream()
                .filter(ri -> ri.getResourceName().endsWith(".stg"))
                .forEach(ri -> System.out.println(ri.getResourceName()));
        var in = this.getClass().getClassLoader().getResourceAsStream("dd/BundleBuilderInterface.stg");
        System.out.println(in);
    }
}

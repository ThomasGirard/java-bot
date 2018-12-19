import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyTest {
	public static void main(String[] args) {
	}

	public static void fixEncoding() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get("data/log.csv"));
		String doubleEncoded = new String(bytes, "utf-8");
		byte[] bytes2 = doubleEncoded.getBytes("iso-8859-1");
		Files.write(Paths.get("data/log-fixed.csv"), bytes2);
	}

}

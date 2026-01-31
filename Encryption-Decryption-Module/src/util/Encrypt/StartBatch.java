package util.Encrypt;

public class StartBatch {

	public static void main(String[] args) {
		try {

			String srcFile = "C:\\Users\\jiming\\Desktop\\code\\암호화 테스트\\202601311827.png";
			Encrypt.encryptFile(srcFile, srcFile + "_enc");
			Decrypt.decryptFile(srcFile + "_enc", srcFile + "_dec");

		} catch (Exception e) {
			System.out.println("Error log: " + e.getMessage());
		}

	}
}

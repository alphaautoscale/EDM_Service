package util.Encrypt;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import config.ConfReader;

/**
 * AES-256 암호화/복호화 유틸리티 클래스
 */
public class Encrypt {

	private static final String FIXED_KEY = ConfReader.getKey("FIXED_KEY");
	static final String ALGORITHM = ConfReader.getKey("ALGORITHM");
	static final String TRANSFORMATION = ConfReader.getKey("TRANSFORMATION");
	static final int IV_LENGTH = Integer.parseInt(ConfReader.getKey("IV_LENGTH"));

	/**
	 * 랜덤 IV(Initialization Vector) 생성
	 * 
	 * @return 16바이트 IV
	 */
	private static byte[] generateIV() {
		byte[] iv = new byte[IV_LENGTH];
		new SecureRandom().nextBytes(iv);
		return iv;
	}

	/**
	 * 데이터 암호화
	 * 
	 * @param plainText 암호화할 평문
	 * @param key       Base64로 인코딩된 키
	 * @return Base64로 인코딩된 암호화된 데이터 (IV + 암호문)
	 */
	public static String encrypt(String plainText, String key) throws Exception {
		// 키 디코딩
		byte[] keyBytes = Base64.getDecoder().decode(key);
		SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);

		// IV 생성
		byte[] iv = generateIV();
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

		// 암호화
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

		// IV + 암호문을 합쳐서 Base64로 인코딩
		byte[] encryptedWithIv = new byte[IV_LENGTH + encryptedBytes.length];
		System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH);
		System.arraycopy(encryptedBytes, 0, encryptedWithIv, IV_LENGTH, encryptedBytes.length);

		return Base64.getEncoder().encodeToString(encryptedWithIv);
	}

	public static byte[] encryptBytes(byte[] data) throws Exception {
		return encryptBytes(data, FIXED_KEY);
	}

	/**
	 * 바이트 배열 암호화 (파일용)
	 * 
	 * @param data 암호화할 바이트 배열
	 * @param key  Base64로 인코딩된 키
	 * @return 암호화된 바이트 배열 (IV + 암호문)
	 */
	public static byte[] encryptBytes(byte[] data, String key) throws Exception {
		// 키 디코딩
		byte[] keyBytes = Base64.getDecoder().decode(key);
		SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);

		// IV 생성
		byte[] iv = generateIV();
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

		// 암호화
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] encryptedBytes = cipher.doFinal(data);

		// IV + 암호문을 합치기
		byte[] encryptedWithIv = new byte[IV_LENGTH + encryptedBytes.length];
		System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH);
		System.arraycopy(encryptedBytes, 0, encryptedWithIv, IV_LENGTH, encryptedBytes.length);

		return encryptedWithIv;
	}

	/**
	 * 파일 암호화
	 * 
	 * @param inputFilePath  암호화할 파일 경로
	 * @param outputFilePath 암호화된 파일을 저장할 경로
	 * @param key            Base64로 인코딩된 키
	 */
	public static void encryptFile(String srcFile, String outputFile) throws Exception {
		// 파일 읽기
		byte[] fileData = Files.readAllBytes(Paths.get(srcFile));
		// 암호화
		byte[] encryptedData = encryptBytes(fileData, FIXED_KEY);

		// 암호화된 데이터를 파일로 저장
		Files.write(Paths.get(outputFile), encryptedData);

		System.out.println("File Encrypt Success: " + srcFile + " -> " + outputFile);
	}
}
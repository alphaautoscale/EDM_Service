package util.Encrypt;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import config.ConfReader;

/**
 * AES-256 복호화 유틸리티 클래스
 */
public class Decrypt {

	private static final String FIXED_KEY = ConfReader.getKey("FIXED_KEY");

	/**
	 * 데이터 복호화
	 * 
	 * @param encryptedText Base64로 인코딩된 암호화된 데이터
	 * @param key           Base64로 인코딩된 키
	 * @return 복호화된 평문
	 */
	public static String decrypt(String encryptedText, String key) throws Exception {
		// 키 디코딩
		byte[] keyBytes = Base64.getDecoder().decode(key);
		SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, Encrypt.ALGORITHM);

		// 암호화된 데이터 디코딩
		byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

		// IV와 암호문 분리
		byte[] iv = new byte[Encrypt.IV_LENGTH];
		byte[] encryptedBytes = new byte[encryptedWithIv.length - Encrypt.IV_LENGTH];
		System.arraycopy(encryptedWithIv, 0, iv, 0, Encrypt.IV_LENGTH);
		System.arraycopy(encryptedWithIv, Encrypt.IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

		// 복호화
		Cipher cipher = Cipher.getInstance(Encrypt.TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

		return new String(decryptedBytes, StandardCharsets.UTF_8);
	}

	/**
	 * 파일 복호화
	 * 
	 * @param inputFilePath  복호화할 암호화된 파일 경로
	 * @param outputFilePath 복호화된 파일을 저장할 경로
	 * @param key            Base64로 인코딩된 키
	 */
	public static void decryptFile(String srcFile, String outputFilePath) throws Exception {

		// 암호화된 파일 읽기
		byte[] encryptedData = Files.readAllBytes(Paths.get(srcFile));

		// 복호화
		byte[] decryptedData = decryptBytes(encryptedData);

		// 복호화된 데이터를 파일로 저장
		Files.write(Paths.get(outputFilePath), decryptedData);

		System.out.println("File Decrpyt Success: " + srcFile + " -> " + outputFilePath);
	}

	/**
	 * 바이트 배열 복호화 (파일용)
	 * 
	 * @param encryptedData 암호화된 바이트 배열 (IV + 암호문)
	 * @param key           Base64로 인코딩된 키
	 * @return 복호화된 바이트 배열
	 */
	public static byte[] decryptBytes(byte[] encryptedData) throws Exception {

		byte[] sigBytes = Encrypt.SIGNATURE.getBytes(StandardCharsets.UTF_8);
		int sigLen = sigBytes.length;
		int ivLen = Encrypt.IV_LENGTH;

		// 1. 키 준비 (지민님 원본 로직)
		byte[] keyBytes = Base64.getDecoder().decode(FIXED_KEY);
		SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, Encrypt.ALGORITHM);

		// 2. IV 떼어내기 (서명 바로 뒤부터 16바이트)
		byte[] iv = new byte[ivLen];
		// 위치 설명: encryptedData의 'sigLen' 번 위치부터 'ivLen'만큼 복사해서 'iv' 배열에 담아라!
		System.arraycopy(encryptedData, sigLen, iv, 0, ivLen);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

		// 3. 진짜 암호문 떼어내기 (서명 + IV 이후부터 끝까지)
		int cipherTextStart = sigLen + ivLen; // 암호문이 시작되는 지점
		int cipherTextLength = encryptedData.length - cipherTextStart; // 전체 길이에서 앞부분 뺀 길이

		byte[] cipherText = new byte[cipherTextLength];
		System.arraycopy(encryptedData, cipherTextStart, cipherText, 0, cipherTextLength);

		// 4. 복호화 실행
		Cipher cipher = Cipher.getInstance(Encrypt.TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

		return cipher.doFinal(cipherText);
	}
}
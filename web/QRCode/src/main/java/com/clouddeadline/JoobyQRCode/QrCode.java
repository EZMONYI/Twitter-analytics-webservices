package com.clouddeadline.JoobyQRCode;

import java.util.ArrayList;
import java.util.List;

// Initially written by Yifan Zhang, duplicated here to be used by Jooby framework
public class QrCode {
	private final int size;
	private final List<Integer> alignmentX;
	private final List<Integer> alignmentY;
	private int[] zigzagX;
	private int[] zigzagY;
	private List<Integer> fixedX;
	private List<Integer> fixedY;
	private final int[][] initQRCode;
	private final int[] paddingPattern;

	public QrCode(int size, List<Integer> alignmentX, List<Integer> alignmentY) {
		this.size = size;
		this.alignmentX = alignmentX;
		this.alignmentY = alignmentY;
		this.initQRCode = new int[size][size];
		this.paddingPattern = new int[]{1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1};
		initialize();
	}

	public String encode(String message) {
		int[] code = new int[size * size];
		// Copy the fixed patterns.
		for (int i = 0; i < size; i++) {
			System.arraycopy(initQRCode[i], 0, code, i * size, size);
		}
		// Fill in value in zigzag pattern.
		// Fill in length.
		int count = 0;
		int length = message.length();
		for (int i = 7; i >= 0; i--) {
			code[zigzagX[count] * size + zigzagY[count]] = (length >> i) & 1;
			count += 1;
		}
		// Fill in characters and check sum.
		for (char letter : message.toCharArray()) {
			int errCode = 0;
			for (int i = 7; i >= 0; i--) {
				int tmp = ((int) letter >> i) & 1;
				code[zigzagX[count] * size + zigzagY[count]] = tmp;
				errCode = errCode ^ tmp;
				count += 1;
			}
			count += 7;
			code[zigzagX[count] * size + zigzagY[count]] = errCode;
			count += 1;
		}
		// Pad remaining cells.
		for (int cur = 0; count < zigzagX.length; cur++) {
			code[zigzagX[count] * size + zigzagY[count]] = paddingPattern[cur % paddingPattern.length];
			count += 1;
		}

		// Use logistic map to encrypt the QR code.
		encrypt(code, size);
		return generateString(code);
	}

	public String decode(String message) {
		String[] nums = message.split("0x", 0);
		if (nums.length != 33 || !nums[0].equals("")) {
			return null;
		}

		int[][] qrCode = new int[32][32];
		int [] tmp = new int[32 * 32];
		// Fill in the QR code.
		for (int i = 0; i < 32; i++) {
			try {
				long num = Long.parseLong(nums[i+1], 16);
				for (int j = 31; j >= 0; j--) {
					tmp[i * 32 + j] = (int) (num % 2);
					num = num / 2;
				}
			} catch(Exception e) {
				return null;
			}
		}
		// Apply logistic map to decode.
		encrypt(tmp, 32);
		for (int i = 0; i < 32; i++) {
			System.arraycopy(tmp, i * 32, qrCode[i], 0, 32);
		}
		for (int i = 0; i < 4; i++) {
			String decodedMessage = decodePattern(qrCode);
			if (decodedMessage != null) {
				return decodedMessage;
			}
			if (i == 3) {
				break;
			}
			qrCode = rotate(qrCode);
		}
		return null;
	}

	private String decodePattern(int[][] code) {
		for (int i = 0; i <= 32 - size; i++) {
			for (int j = 0; j <= 32 - size; j++) {
				if (detectPattern(code, i, j)) {
					int wordNum = 0;
					StringBuilder decodedString = new StringBuilder();
					for (int x = 0; x < 8; x++) {
						wordNum *= 2;
						wordNum += code[i+zigzagX[x]][j+zigzagY[x]];
					}
					for (int k = 0; k < wordNum; k++) {
						int start = 8 + k * 16;
						int asciiCode = 0;
						for (int x = 0; x < 8; x++) {
							asciiCode *= 2;
							asciiCode += code[i+zigzagX[start+x]][j+zigzagY[start+x]];
						}
						decodedString.append((char) asciiCode);
					}
					return decodedString.toString();
				}
			}
		}
		return null;
	}

	private int[][] rotate(int[][] code) {
		int[][] rotatedCode = new int[32][32];
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				rotatedCode[i][j] = code[j][31-i];
			}
		}
		return rotatedCode;
	}

	private boolean detectPattern(int[][] qrCode, int x, int y) {
		for (int i = 0; i < fixedX.size(); i++) {
			if (qrCode[x+fixedX.get(i)][y+fixedY.get(i)] != initQRCode[fixedX.get(i)][fixedY.get(i)]) {
				return false;
			}
		}
		return true;
	}

	private void encrypt(int[] code, int length) {
		double x = 0.1;
		double r = 4.0;
		for (int i = 0; i < length * length / 8; i++) {
			int logisticValue = (int) (x * 255);
			for (int j = i * 8; j < i * 8 + 8; j++) {
				code[j] = code[j] ^ (logisticValue & 1);
				logisticValue = logisticValue >> 1;
			}
			x = r * x * (1 - x);
		}
		int logisticValue = (int) (x * 255);
		for (int i = length * length - (length * length) % 8; i < length * length; i++) {
			code[i] = code[i] ^ (logisticValue & 1);
			logisticValue = logisticValue >> 1;
		}
	}

	private String generateString(int[] code) {
		StringBuilder string = new StringBuilder();
		for (int i = 0; i < size * size / 32; i++) {
			string.append("0x");
			int cur = 0;
			for (int j = i * 32; j < i * 32 + 32; j++) {
				cur = cur << 1;
				cur += code[j];
			}
			string.append(Integer.toHexString(cur));
		}
		int cur = 0;
		for (int i = size * size - (size * size) % 32; i < size * size; i++) {
			cur = cur << 1;
			cur += code[i];
		}
		string.append("0x");
		string.append(Integer.toHexString(cur));

		return string.toString();
	}

	private void initialize() {
		int restCellNum = size * size - 64 * 3 - 5 * 6 * alignmentX.size() - 2 * 8 - 2 * (size - 16) + 1;
		this.zigzagX = new int[restCellNum];
		this.zigzagY = new int[restCellNum];
		this.fixedX = new ArrayList<>();
		this.fixedY = new ArrayList<>();

		// Initialize initQRCode.
		// Add three position detection patterns.
		drawSquare(3, 3, 1);
		drawSquare(3, 3, 3);
		drawSquare(3, 3, 7);
		drawSquare(3, size - 4, 1);
		drawSquare(3, size - 4, 3);
		drawSquare(3, size - 4, 7);
		drawSquare(size - 4, 3, 1);
		drawSquare(size - 4, 3, 3);
		drawSquare(size - 4, 3, 7);

		// Add fixed pattern of the detection patterns.
		addFixedPattern(3, 3, 1);
		addFixedPattern(3, 3, 3);
		addFixedPattern(3, 3, 5);
		addFixedPattern(3, 3, 7);
		addFixedPattern(3, size - 4, 1);
		addFixedPattern(3, size - 4, 3);
		addFixedPattern(3, size - 4, 5);
		addFixedPattern(3, size - 4, 7);
		addFixedPattern(size - 4, 3, 1);
		addFixedPattern(size - 4, 3, 3);
		addFixedPattern(size - 4, 3, 5);
		addFixedPattern(size - 4, 3, 7);
		for (int i = 0; i < 9; i++) {
			fixedX.add(7);
			fixedY.add(i);
			fixedX.add(size - 8);
			fixedY.add(i);
		}
		for (int i = 0; i < 8; i++) {
			fixedX.add(7);
			fixedY.add(size - i - 1);
		}
		for (int i = 0; i < 7; i++) {
			fixedX.add(i);
			fixedY.add(7);
			fixedX.add(i);
			fixedY.add(8);
			fixedX.add(size - 7 + i);
			fixedY.add(7);
			fixedX.add(size - 7 + i);
			fixedY.add(8);
			fixedX.add(i);
			fixedY.add(size - 8);
		}

		// Add alignment patterns.
		for (int i = 0; i < alignmentX.size(); i++) {
			drawSquare(alignmentX.get(i), alignmentY.get(i), 1);
			drawSquare(alignmentX.get(i), alignmentY.get(i), 5);
			addFixedPattern(alignmentX.get(i), alignmentY.get(i), 1);
			addFixedPattern(alignmentX.get(i), alignmentY.get(i), 3);
			addFixedPattern(alignmentX.get(i), alignmentY.get(i), 5);
		}

		// Simulate the zigzag pattern.
		int x = size - 1;
		int y = size - 1;
		int deltaX = -1;
		int deltaY = -1;
		zigzagX[0] = x;
		zigzagY[0] = y;
		boolean isHorizontal = true;
		for (int i = 1; i < restCellNum; i++) {
			if (isHorizontal) {
				y += deltaY;
				isHorizontal = false;
			} else {
				int tmpX = x + deltaX;
				int tmpY = y - deltaY;
				if (isInMatrix(tmpX, tmpY) && notOverlap(tmpX, tmpY)) {
					if (tmpX == 6) {
						tmpX += deltaX;
					}
					if (initQRCode[tmpX][tmpY] == 1) {
						tmpX += deltaX * 5;
					}
					x = tmpX;
					y = tmpY;
				} else {
					// Hit the boundary.
					y += deltaY;
					deltaX *= -1;
					if (y == 6) {
						y -= 1;
					}
					if (y == 8) {
						x -= 8;
					}
				}
				isHorizontal = true;
			}
			zigzagX[i] = x;
			zigzagY[i] = y;
		}

		// Add timing patterns.
		for (int i = 8; i < size - 8; i++) {
			initQRCode[6][i] = 1 - i % 2;
			initQRCode[i][6] = 1 - i % 2;
			fixedX.add(6);
			fixedY.add(i);
			fixedX.add(i);
			fixedY.add(6);
		}

	}

	private void drawSquare(int x, int y, int length) {
		x = x - (length - 1) / 2;
		y = y - (length - 1) / 2;
		for (int i = 0; i < length; i ++) {
			initQRCode[x + i][y] = 1;
			initQRCode[x + i][y + length - 1] = 1;
		}
		for (int i = 1; i < length - 1; i ++) {
			initQRCode[x][y + i] = 1;
			initQRCode[x + length - 1][y + i] = 1;
		}
	}

	private void addFixedPattern(int x, int y, int length) {
		x = x - (length - 1) / 2;
		y = y - (length - 1) / 2;
		for (int i = 0; i < length; i ++) {
			fixedX.add(x + i);
			fixedY.add(y);
			fixedX.add(x + i);
			fixedY.add(y + length - 1);
		}
		for (int i = 1; i < length - 1; i ++) {
			fixedX.add(x);
			fixedY.add(y + i);
			fixedX.add(x + length - 1);
			fixedY.add(y + i);
		}
	}

	private boolean isInMatrix(int x, int y) {
		return x >= 0 && x < size && y >= 0 && y < size;
	}

	private boolean notOverlap(int x, int y) {
		if (x < 8  && y < 9) {
			return false;
		}
		if (x >= size - 8  && y < 8) {
			return false;
		}
		if (x < 8  && y >= size - 8) {
			return false;
		}
		return true;
	}
}

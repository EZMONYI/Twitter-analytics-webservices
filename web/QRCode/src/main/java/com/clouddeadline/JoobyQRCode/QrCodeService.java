package com.clouddeadline.JoobyQRCode;

import java.util.ArrayList;
import java.util.List;

// Initially written by Yifan Zhang, duplicated here to be used by Jooby framework
public class QrCodeService {
	QrCode codeV1;
	QrCode codeV2;

	public QrCodeService() {
		this.codeV1 = new QrCode(21, new ArrayList<Integer>(), new ArrayList<Integer>());
		List<Integer> x = new ArrayList<>();
		List<Integer> y = new ArrayList<>();
		x.add(18);
		y.add(18);
		this.codeV2 = new QrCode(25, x, y);
	}

	public String encode(String message) {
		if (message.length() <= 13) {
			return codeV1.encode(message);
		} else if (message.length() <= 22) {
			return codeV2.encode(message);
		} else {
			return "invalid input";
		}
	}

	public String decode(String message) {
		String v1Decode = codeV1.decode(message);
		if (v1Decode != null) {
			return v1Decode;
		} else {
			String v2Decode = codeV2.decode(message);
			if (v2Decode != null) {
				return v2Decode;
			}
			return "invalid input";
		}
	}
}

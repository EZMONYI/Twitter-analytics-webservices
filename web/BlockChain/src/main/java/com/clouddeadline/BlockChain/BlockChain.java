package com.clouddeadline.BlockChain;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class BlockChain {
	public JSONObject decode(String data) throws RuntimeException {
		Base64.Decoder base64Decoder = Base64.getUrlDecoder();
		byte[] decodedData = base64Decoder.decode(data);

		try {
			Inflater decompresser = new Inflater();
			decompresser.setInput(decodedData, 0, decodedData.length);
			ByteArrayOutputStream blockChainStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			while (!decompresser.finished()) {
				int bytes = decompresser.inflate(buffer);
				blockChainStream.write(buffer, 0, bytes);
			}
			decompresser.end();
			String blockChainString = blockChainStream.toString();
			blockChainStream.close();
			return (JSONObject) new JSONParser().parse(blockChainString);
		} catch(Exception e) {
			throw new RuntimeException("Invalid JSON structure");
		}
	}

	public String encode(JSONObject jsonObject) {
		try {
			// Compress with zlib.
			Deflater compresser = new Deflater();
			compresser.setInput(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
			ByteArrayOutputStream blockChainStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			compresser.finish();
			while (!compresser.finished()) {
				int bytes = compresser.deflate(buffer);
				blockChainStream.write(buffer, 0, bytes);
			}
			compresser.end();
			byte[] compressedInfo = blockChainStream.toByteArray();
			blockChainStream.close();

			// Encode with Base64.
			Base64.Encoder base64Encoder = Base64.getUrlEncoder();
			return base64Encoder.encodeToString(compressedInfo);
		} catch(Exception e) {
			System.out.println(e);
		}
		return "";
	}

	public String validateAndAddBlockChain(String data) {
		JSONObject blockChain;
		try{
			blockChain = decode(data);
		} catch (RuntimeException e){
			return "INVALID";
		}
		if(!isChainValid(blockChain)) {
			return "INVALID";
		}
		addBlock(blockChain);
		return encode(blockChain);
	}

	private void addBlock(JSONObject blockChain) {
		JSONObject newBlock = new JSONObject();
		String target = (String) blockChain.get("new_target");
		JSONArray chain = (JSONArray) blockChain.get("chain");
		int size = chain.size();
		newBlock.put("id", size);
		newBlock.put("target", target);
		JSONArray transactions = (JSONArray) blockChain.get("new_tx");
		for (int i = 0; i < transactions.size(); i++) {
			JSONObject transaction = (JSONObject) transactions.get(i);
			if (transaction.get("send") == null) {
                // Transaction created by the miner.
				transaction.put("fee", 0);
				transaction.put("send", 1097844002039L);
				String hash = getTransactionHash(transaction);
				transaction.put("hash", hash);
				transaction.put("sig", generateSig(hash));
			}
		}
		// Generate reward transaction.
		JSONObject reward = new JSONObject();
		reward.put("recv", 1097844002039L);
		reward.put("amt", 500000000 / Math.pow(2, size / 2));
		JSONObject lastBlock = (JSONObject) chain.get(size - 1);
		JSONArray lastBlockTransactions = (JSONArray) lastBlock.get("all_tx");
		String lastTransactionTime = (String) ((JSONObject) lastBlockTransactions.get(lastBlockTransactions.size() - 1)).get("time");
		reward.put("time", String.valueOf(Long.parseLong(lastTransactionTime) + 600000000000L));
		reward.put("hash", getTransactionHash(reward));
		// Put reward into transactions.
		transactions.add(reward);

		newBlock.put("all_tx", transactions);

		// Start mining.
		StringBuilder prefixBuilder = new StringBuilder();
		prefixBuilder.append(size);
		prefixBuilder.append("|");
		prefixBuilder.append((String) lastBlock.get("hash"));
		for (int i = 0; i < transactions.size(); i++) {
			JSONObject transaction = (JSONObject) transactions.get(i);
			prefixBuilder.append("|");
			prefixBuilder.append((String) transaction.get("hash"));
		}
		String prefix = sha256(prefixBuilder.toString());
		for (int pow = 0; pow < Integer.MAX_VALUE; pow++) {
			String blockHash = ccHash(prefix + String.valueOf(pow));
			if (blockHash.compareTo(target) < 0) {
				newBlock.put("hash", blockHash);
				newBlock.put("pow", String.valueOf(pow));
				break;
			}
		}
		// Remove unnecessary keys.
		blockChain.remove("new_tx");
		blockChain.remove("new_target");

		// Add new block.
		chain.add(newBlock);
	}

	private String getTransactionHash(JSONObject transaction) {
		StringBuilder information = new StringBuilder();
		information.append((String) transaction.get("time"));
		information.append("|");
		if (transaction.get("send") != null) {
			information.append(((Number) transaction.get("send")).longValue());
		}
		information.append("|");
		information.append(((Number) transaction.get("recv")).longValue());
		information.append("|");
		information.append(((Number) transaction.get("amt")).longValue());
		information.append("|");
		if (transaction.get("fee") != null) {
			information.append(((Number) transaction.get("fee")).longValue());
		}
		return ccHash(information.toString());
	}

	private long generateSig(String hash) {
		BigInteger privateKey = BigInteger.valueOf(343710770439L);
		BigInteger n = BigInteger.valueOf(1561906343821L);
		BigInteger hashValue = BigInteger.valueOf(Long.parseLong(hash, 16));
		return hashValue.modPow(privateKey, n).longValue();
	}

	public String ccHash(String data) {
		try {
			MessageDigest encoder = MessageDigest.getInstance("SHA-256");
			byte[] hash = encoder.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hashCode = new StringBuilder();
			for (int i = 0; i < 4; i++) {
				hashCode.append(String.format("%02x", hash[i]));
			}
			return hashCode.toString();
		} catch (Exception e) {
            System.out.println(e);
		}
		return "";
	}

	public String sha256(String data) {
		try {
			MessageDigest encoder = MessageDigest.getInstance("SHA-256");
			byte[] hash = encoder.digest(data.getBytes(StandardCharsets.UTF_8));
			StringBuilder sha256Code = new StringBuilder();
			for (int i = 0; i < hash.length; i++) {
				sha256Code.append(String.format("%02x", hash[i]));
			}
			return sha256Code.toString();
		} catch (Exception e) {
			System.out.println(e);
		}
		return "";
	}

	boolean isChainValid(JSONObject blockObject){
		// structure looks right
		if(!blockObject.containsKey("chain")){
			return false;
		}
		if(!blockObject.containsKey("new_target")){
			return false;
		}
		if(!blockObject.containsKey("new_tx")){
			return false;
		}


		JSONArray chain =(JSONArray) blockObject.get("chain");

		// Check for time
		long previousTimestamp = 0;
		for (int i = 0; i < chain.size(); i++){
			JSONObject block = (JSONObject) chain.get(i);
			JSONArray transactions = (JSONArray) block.get("all_tx");
			for(int j=0; j<transactions.size(); j++){
				JSONObject transaction = (JSONObject)transactions.get(j);
				long timestamp = Long.parseLong(((String) transaction.get("time")));
				if(previousTimestamp >= timestamp) {
					return false;
				}
				previousTimestamp = timestamp;
			}
		}

		// Check block id increment
		// look into chain
		String previousHash = "00000000";
		for (int i = 0; i<chain.size(); i++) {
			JSONObject block = (JSONObject) chain.get(i);
			long blockId = ((Number) block.get("id")).longValue();
			if(blockId != i){
				return false;
			}
			if(!isBlockValid(block,previousHash)){
				return false;
			}
			// update previous hash
			previousHash = (String) block.get("hash");
		}
		// Check for time out of bound

		// Check new transactions
		JSONArray newTransactions =(JSONArray) blockObject.get("new_tx");
		if(newTransactions.isEmpty()){
			return true;
		}
		for (int i = 0; i < newTransactions.size(); i++) {
			JSONObject transaction = (JSONObject) newTransactions.get(i);
			long timestamp = Long.parseLong(((String) transaction.get("time")));
			if(previousTimestamp >= timestamp) {
				return false;
			}
			previousTimestamp = timestamp;
		}
		return true;
	}

	private boolean isBlockValid(JSONObject block, String previousHash) {
		// Block hash is valid
		String blockHash = (String) block.get("hash");
		String blockId = String.valueOf(((Number) block.get("id")).longValue());
		JSONArray transactions = (JSONArray) block.get("all_tx");
		StringBuilder prefixBuilder = new StringBuilder();
		prefixBuilder.append(blockId);
		prefixBuilder.append("|");
		prefixBuilder.append(previousHash);
		for (Object o : transactions) {
			JSONObject transaction = (JSONObject) o;
			prefixBuilder.append("|");
			prefixBuilder.append((String) transaction.get("hash"));
		}
		String pow = (String) block.get("pow");
		String generatedHash = ccHash(sha256(prefixBuilder.toString())+pow);
		if(!blockHash.equals(generatedHash)){
			return false;
		}
		String target = (String) block.get("target");
		if (blockHash.compareTo(target) >= 0) {
			return false;
		}
		// Check Reward Amount
		long rightAmount = (long) (500000000 / Math.pow(2, Long.parseLong(blockId) / 2));
		JSONObject reward = (JSONObject) transactions.get(transactions.size()-1);
		long amount = ((Number) reward.get("amt")).longValue();
		if(amount != rightAmount){
			return false;
		}

		// Check Reward is not normal transaction
		if(reward.get("send") != null){
			return false;
		}
		if(reward.get("sig") != null){
			return false;
		}
		if(reward.get("fee") != null){
			return false;
		}

		// Transaction hash
		for (int i = 0; i < transactions.size()-1; i++){
			JSONObject transaction = (JSONObject) transactions.get(i);
			if(!isTransactionValid(transaction)){
				return false;
			}
		}
		return true;
	}

	public boolean isTransactionValid(JSONObject transaction) {
		// check signature
		if(!isSignatureValid(transaction)){
			return false;
		}
		// cast to number first
		String timestamp = (String) transaction.get("time");
		String sender = String.valueOf(((Number) transaction.get("send")).longValue());
		String recipient = String.valueOf(((Number) transaction.get("recv")).longValue());
		String amount = String.valueOf(((Number) transaction.get("amt")).longValue());
		if(transaction.get("fee") == null){
			return false;
		}
		String fee = String.valueOf(((Number) transaction.get("fee")).longValue());
		StringBuilder transactionInfo = new StringBuilder();

		// check for non-negative fee
		if(((Number) transaction.get("fee")).longValue() < 0){
			return false;
		}
		// check for non-negative amount
		if(((Number) transaction.get("amt")).longValue() < 0){
			return false;
		}

		transactionInfo.append(timestamp);
		transactionInfo.append("|");
		transactionInfo.append(sender);
		transactionInfo.append("|");
		transactionInfo.append(recipient);
		transactionInfo.append("|");
		transactionInfo.append(amount);
		transactionInfo.append("|");
		transactionInfo.append(fee);
		String transactionHash = (String) transaction.get("hash");
		if(!transactionHash.equals(ccHash(transactionInfo.toString()))){
			return false;
		}
		return true;
	}

	public boolean isSignatureValid(JSONObject transaction) {
		// input signature, and n, sender public key
		// n = 1561906343821
		// signature = sig
		// sender public key = sender account
		// output hash
		// check if output hash = transaction hash
		if(transaction.get("send") == null){
			return false;
		}
		BigInteger publicKey = BigInteger.valueOf(((Number) transaction.get("send")).longValue());
		if(transaction.get("sig") == null){
			return false;
		}
		BigInteger sig = BigInteger.valueOf(((Number) transaction.get("sig")).longValue());
		BigInteger n = BigInteger.valueOf(1561906343821L);
		if(transaction.get("hash") == null){
			return false;
		}
		long hash = Long.parseLong(((String) transaction.get("hash")),16);
		long generatedHash = sig.modPow(publicKey,n).longValue();
		if(hash != generatedHash){
			return false;
		}
		return true;
	}
}

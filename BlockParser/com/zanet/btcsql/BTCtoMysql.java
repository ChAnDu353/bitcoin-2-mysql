package com.zanet.btcsql;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Arrays; 
import java.util.Locale;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.utils.BlockFileLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class BTCtoMysql {

	String blockhash = "xx";
	List<File> list;
	String blockdate;
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	ConManager dbcon = null;
	public static Connection con = null;

	int maxInsertBatch = 6000;
 
	public static void main(String[] args) {
		// BTCtoMysql btm = new BTCtoMysql();
		// tb.LoadBlkFiles(1);
		// btm.LoadBlock(1);

	}
 

	public int[] LoadBlock(String blocks_path, int block_file, int[] counters) {

		int[] counts = new int[2];
		int blockCounter = counters[0];
		int trid = counters[1];

		try {

			int blockfile = block_file;

			int tm = 0;

			dbcon = new ConManager();
			con = dbcon.getConnection();

			NetworkParameters np = new MainNetParams();
			Context.getOrCreate(MainNetParams.get());

			long startTime = System.currentTimeMillis();
		 
			File blockFile = new File(blocks_path + String.format(Locale.US, "blk%05d.dat", block_file));
			BlockFileLoader loader = new BlockFileLoader(np, Arrays.asList(blockFile));
 

			con.setAutoCommit(false);
			con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			PreparedStatement tr = con.prepareStatement(
					"insert into transactions ( trid, tr_hash,block_id,block_file ) values( ?, ?,?,? )");
			PreparedStatement intrx = con.prepareStatement("insert into trxin (  trid, txin_address , invalue, block_file ) values( ?,?,?,? )");  
			PreparedStatement outtrx = con.prepareStatement( "insert into trxout (  trid, txout_address , outvalue, block_file ) values( ?,?,? ,?)");  
			PreparedStatement upBlk = con.prepareStatement(	"insert into blocks ( block_id,block_hash,block_date,block_file) values( ?,?,?,?)");

			for (Block block : loader) {
				tm++;
				blockCounter++;

				blockdate = format.format(block.getTime());
 
					for (Transaction tx : block.getTransactions()) { 
						trid++;

						tr.setInt(1, trid);
						tr.setString(2, tx.getTxId().toString());
						tr.setInt(3, blockCounter);
						tr.setInt(4, blockfile);
						tr.addBatch();

						if (tx.isCoinBase()) {
							// System.out.println("COINBASE");
						} else {
							try {
								List<TransactionInput> inputs = tx.getInputs();
								for (TransactionInput inTx : inputs) {

									List<ScriptChunk> chunks = inTx.getScriptSig().getChunks();

									for (int k = 1; k < chunks.size(); k++) {
										String fromAddress = "address";
										String adr = chunks.get(k).toString();

										if (adr.indexOf("[") > 0) {
											adr = adr.substring(adr.indexOf("[") + 1, adr.indexOf("]"));
											fromAddress = new ScriptToPublicAddress().getAddress(adr);
										} else {
											System.out.println("adr " + adr);
											System.out.println("Tx Hex: " + tx.getTxId());
										}

										double inval = tx.getInputSum().getValue() / 100000000D;

										intrx.setInt(1, trid);
										intrx.setString(2, fromAddress.toString());
										intrx.setDouble(3, inval);
										intrx.setInt(4, blockfile);
										// intrx.setInt(5, blockCounter );
										intrx.addBatch();
									}

								}

							} catch (final ScriptException x) {
								System.out.println(x.getMessage());
							}
						}

						List<TransactionOutput> outputs = tx.getOutputs();
						for (TransactionOutput outTx : outputs) {

							try {

								Script script = outTx.getScriptPubKey();
								String toAddress = script.getToAddress(np, true).toString();

								double outval = outTx.getValue().value / 100000000D;

								outtrx.setInt(1, trid);
								outtrx.setString(2, toAddress);
								outtrx.setDouble(3, outval);
								outtrx.setInt(4, blockfile);
								// outtrx.setInt(5, blockCounter );
								outtrx.addBatch();

							} catch (Exception ignored) {
							}

						}
 
					} // End Transaction

					upBlk.setInt(1, blockCounter);
					upBlk.setString(2, block.getHashAsString());
					upBlk.setString(3, blockdate);

					upBlk.setInt(4, blockfile);
				 
					upBlk.addBatch();

					if (tm == maxInsertBatch) {
					 
						long strtm1 = System.currentTimeMillis();
						tr.executeBatch();
						intrx.executeBatch();
						outtrx.executeBatch();
						upBlk.executeBatch();

						tm = 0;
						long endtm1 = System.currentTimeMillis();
						printDifference(strtm1, endtm1, "batchexecute of " + maxInsertBatch);
					}
 

			} // blockloader

			tr.executeBatch();
			intrx.executeBatch();
			outtrx.executeBatch();
			upBlk.executeBatch();
			con.commit();

			con.setAutoCommit(false);
			con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			PreparedStatement bkfl = con.prepareStatement("insert into blockfiles ( block_file ) values( ? )");

			bkfl.setInt(1, blockfile);

			bkfl.executeUpdate();
			con.commit();
			con.close();

			long endTime = System.currentTimeMillis();
		 
			printDifference(startTime, endTime, "Block time ");
 
			trid++;

			BufferedWriter writer = new BufferedWriter(new FileWriter("trid.txt", false));
			String last_tr_id = "" + trid;
			writer.write(last_tr_id);
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		counts[0] = blockCounter++;
		counts[1] = trid;
		return counts;
	} // end of loadBlock() method.

	public void printDifference(long startTime, long endTime, String msg) {
		
		long different = endTime - startTime;
		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
	 
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different / secondsInMilli;
		System.out.print(msg);
	 
		System.out.printf(" %d minutes, %d seconds%n", elapsedMinutes, elapsedSeconds);
	}

}
package com.zanet.btcsql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

public class BTCSqlParser {
	static String BLOCKSPATH = "C:\\Bitcoin\\core\\blocks\\";
	static FileWriter fw;

	public static void main(String[] args) {
		int fileid = 0;
		int num_files_to_process = 1000;
		int trd = 1, blkcountr = 1;
		SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		try {
			fw = new FileWriter("log.txt", true);

			Properties props = new Properties();
			FileInputStream in = new FileInputStream("db.properties");
			props.load(in);
			num_files_to_process = Integer.parseInt(props.getProperty("num_files"));
			BLOCKSPATH = props.getProperty("blocks_path");

			File trfile = new File("trid.txt");
			BufferedReader br = new BufferedReader(new FileReader(trfile));

			String last_trid = br.readLine();
			System.out.println("last_trid " + last_trid.trim());

			last_trid = last_trid.replaceAll("[\n\r]", "");
			trd = Integer.parseInt(last_trid.trim());
			br.close();

			System.out.println("Program started " + dateformat.format(Calendar.getInstance().getTime()));
			System.out.println("Files to process " + num_files_to_process);
			fw.append("Program started " + dateformat.format(Calendar.getInstance().getTime()));
			fw.append("\n");
		} catch (Exception nfe) {
			System.out.println("Check trid.txt");
			nfe.printStackTrace();
			System.exit(1);
		}

		try {

			ConManager dbcon = new ConManager();
			Connection con = dbcon.getConnection();

			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("select max(block_file) from blockfiles ");
			if (rs.next()) {
				fileid = 1 + rs.getInt(1);
			}
			rs.close();
			con.close();

			int[] counters = new int[2];
			counters[0] = blkcountr;
			counters[1] = trd;

			System.out.println(counters[0] + " counters " + counters[1]);

			for (int k = fileid; k < fileid + num_files_to_process; k++) {
				File blockFile = new File(BLOCKSPATH + String.format(Locale.US, "blk%05d.dat", k));

				if (blockFile.exists()) {
 
					System.out.println("File loaded " + BLOCKSPATH + String.format(Locale.US, "blk%05d.dat", k));
					BTCtoMysql tb = new BTCtoMysql();
					counters = tb.LoadBlock(BLOCKSPATH, k, counters);
				 
				}

			}
			fw.append(fileid + " block file started " + dateformat.format(Calendar.getInstance().getTime()));
			fw.append("\n");
			fw.close();

			System.out.println("END of Importing DATA blocks");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jxl.Sheet;
import jxl.Workbook;


/**
 *@desc   java实现一键打包工具
 *@author zhengjun
 *@created at 2018/5/4 16:59
*/
public class ApkChannel02 {

	private static final String versionName = "_1.1.1.apk";
	//private static final String appName = "ckjr";
    //private static final String appName = "hos";
	//private static final String appName = "qsl";
	private static final String appName = "fund";
	//private static final String apkFilePath = "F:/渠道打包/";
	private static final String apkFilePath = "F:/fundPackge/";
	//事先准备好的打包apk，根据这个apk生成不同平台的apk，放在事先建好的这件夹中
	//private static final String apkPath = apkFilePath+appName+versionName;
	private static final String apkPath = apkFilePath+appName+versionName;

    private static LinkedBlockingQueue<String> mQueue = new LinkedBlockingQueue<>();

	public static void main(String[] args) {
		try {
			//ckjr.xls 是所有要打包的平台渠道名
			// Workbook book = Workbook.getWorkbook(new FileInputStream(new File("F:/ckjr.xls")));
			Workbook book = Workbook.getWorkbook(new FileInputStream(new File("F:/fund.xls")));
			//Workbook book = Workbook.getWorkbook(new FileInputStream(new File("F:/qsl.xls")));
            //Workbook book = Workbook.getWorkbook(new FileInputStream(new File("F:/hos.xls")));
			Sheet sheet = book.getSheet(0);
			for (int i = 1; i < sheet.getRows(); i++) {
				String contents = sheet.getCell(1, i).getContents();
				if (contents != null && !"".equals(contents.trim())) {
					mQueue.offer(contents);					
				}
			}
			book.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		File dfile = new File(apkFilePath + appName);
		if(!dfile.exists())
			dfile.mkdirs();
		String path = dfile.getPath();
		int index = 0;
		while(index++<15) {
			new Thread(() -> new ApkChannel02().startCopy(path)).start();
		}
	}
	
	private void startCopy(String outPath) {
		try {
			String channel;
			while((channel=mQueue.poll(1, TimeUnit.SECONDS))!=null) {
				File file = new File(apkPath);
				FileOutputStream fos = new FileOutputStream(outPath+"/"+appName+"_" + channel +versionName);
				ZipFile zipFile = new ZipFile(file);
				ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos);
				Enumeration<ZipArchiveEntry> entries =  zipFile.getEntries();
				while(entries.hasMoreElements()) {
					ZipArchiveEntry entry = entries.nextElement();
					zos.putArchiveEntry(entry);
//					zos.putArchiveEntry(new ZipArchiveEntry(entry.getName()));
					int length;
					byte[] buffer = new byte[1024];
					InputStream is = zipFile.getInputStream(entry);
					while((length=is.read(buffer))!=-1) {
						zos.write(buffer, 0, length);
					}
					is.close();
					buffer = null;
				}
				zos.putArchiveEntry(new ZipArchiveEntry("META-INF/channel_" + channel));
				zos.closeArchiveEntry();
				zos.close();
				System.out.println("剩余" + mQueue.size());
			}
			if(mQueue.size()==0) {
				openOutputFile();
//				System.out.println("done");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean opened = false;
	private void openOutputFile() {
		synchronized (this) {
			if(opened) {
				return;
			}
			opened = true;
		}
		String[] cmd = new String[5];  
        cmd[0] = "cmd";  
        cmd[1] = "/c";  
        cmd[2] = "start";  
        cmd[3] = " ";  
        cmd[4] = apkFilePath;
        try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

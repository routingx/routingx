package routingx.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @see 使用ffmpeg将MP4视频转为H264编码
 * @see 视频编码格式转换
 * @see 比如一个视频的编码是MPEG4，想用H264编码，咋办？
 * @see ffmpeg -i input.mp4 -vcodec h264 output.mp4
 * @see 相反也一样
 * @see ffmpeg -i input.mp4 -vcodec mpeg4 output.mp4
 * 
 * @author Administrator
 *
 */
public class MediocreExecJavac {
	public static boolean transfer(String infile, String outfile) {
//		String avitoflv = "ffmpeg -i " + infile + " -ar 22050 -ab 56 -f flv -y -s 320x240 " + outfile;
//
//		String flvto3gp = "ffmpeg -i " + infile
//				+ " -ar 8000 -ac 1 -acodec amr_nb -vcodec h263 -s 176x144 -r 12 -b 30 -ab 12 " + outfile;
//
//		String avito3gp = "ffmpeg -i " + infile
//				+ " -ar 8000 -ac 1 -acodec amr_nb -vcodec h263 -s 176x144 -r 12 -b 30 -ab 12 " + outfile;
//
//		String avitojpg = "ffmpeg -i " + infile + " -y -f image2 -ss 00:00:10 -t 00:00:01 -s 350x240 " + outfile;

		String videoCommend = "ffmpeg -i " + infile + " -vcodec libx264 -r 29.97 -b 768k -ar 24000 -ab 64k -s 1280x720 "

				+ outfile;

		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(videoCommend);
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;

			while ((line = br.readLine()) != null)

				System.out.println(line);

			int exitVal = proc.waitFor();

			System.out.println("Process exitValue: " + exitVal);

		} catch (Throwable t) {
			t.printStackTrace();

			return false;

		}

		return true;

	}
}
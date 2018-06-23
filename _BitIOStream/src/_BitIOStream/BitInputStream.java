package _BitIOStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream {
	BufferedInputStream bis;	// 输入流
	private byte byte_cur=0;	// 当前处理中的字节
	private int pos_next=8;		// 下一个处理的位置
	
	// 构造函数（使用File）
	public BitInputStream(String fileName) throws IOException {
		bis=new BufferedInputStream(new FileInputStream(fileName));
	}
	
	// 构造函数（使用File）
	public BitInputStream(File file) throws IOException {
		bis=new BufferedInputStream(new FileInputStream(file));
	}

	// 构造函数（使用InputStream）
	public BitInputStream(InputStream inputStream) {
		bis=new BufferedInputStream(inputStream);
	}

	// 从流中读取一位数据
	public int read() throws IOException {
		if (pos_next==8) {
			if (bis.available()<=0) {
				return -1;
			}
			byte_cur=(byte) bis.read();
			pos_next=0;
		}
		if (((byte_cur>>7)&0x01)==0) {
			byte_cur<<=1;
			pos_next++;
			return 0;
		} else {
			byte_cur<<=1;
			pos_next++;
			return 1;
		}
	}
	
	// 从流中读取一组数据保存于数组中，返回实际读取的位数
	public int read(int[] buf) throws IOException {
		int count=0;
		for (int i = 0; i < buf.length; i++) {
			if (available()==0) {
				return count;
			}
			buf[i]=read();
			count++;
		}
		return count;
	}
	
	// 读取指定长度的位数保存在起始偏移量向后的空间内，返回实际读取的位数
	public int read(int[] buf,int off,int len) throws IOException {
		if (len<0||off<0||off+len>buf.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		int count=0;
		for (int i = off; i < len+off; i++) {
			if (available()==0) {
				return count;
			}
			buf[i]=read();
			count++;
		}
		return count;
	}
	
	// 跳过流中接下来的几位，返回实际跳过的位数
	public long skip(long n) throws IOException {
		int count=0;
		for (int i = 0; i < n; i++) {
			if (available()==0) {
				return count;
			}
			read();
			count++;
		}
		return count;
	}
	
	// 获取流中剩余可读位数
	public long available() throws IOException {
		return (bis.available()+1)*8-pos_next;
	}
	
	// 关闭流
	public void close() throws IOException {
		bis.close();
		byte_cur=0;
		pos_next=8;
	}
}

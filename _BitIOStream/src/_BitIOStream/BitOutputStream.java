package _BitIOStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream {
	BufferedOutputStream bos;	// 输出流
	private byte byte_cur=0;	// 当前处理中的字节
	private int pos_next=0;		// 下一个处理的位置
	
	// 构造函数（使用File）
	public BitOutputStream(String fileName) throws IOException {
		bos=new BufferedOutputStream(new FileOutputStream(fileName));
	}
	
	// 构造函数（使用File）
	public BitOutputStream(File file) throws IOException {
		bos=new BufferedOutputStream(new FileOutputStream(file));
	}

	// 构造函数（使用OutputStream）
	public BitOutputStream(OutputStream OutputStream) {
		bos=new BufferedOutputStream(OutputStream);
	}
	
	// 向流中写入一位数据，若buf小于0则作为0处理，若buf大于1则作为1处理
	public void write(int buf) throws IOException {
		if (buf<=0) {
			byte_cur<<=1;
		} else {
			byte_cur=(byte) ((byte_cur<<1)|0x01);
		}
		if (++pos_next==8) {
			bos.write(byte_cur);
			pos_next=0;
			byte_cur=0;
		}
	}
	
	// 向流中写入一组数据
	public void write(int[] buf) throws IOException {
		for (int i = 0; i < buf.length; i++) {
			write(buf[i]);
		}
	}
	
	// 	从起始偏移量向后的空间内读取指定长度的数据并写入流中
	public void write(int[] buf,int off,int len) throws IOException {
		if (len<0||off<0||off+len>buf.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		for (int i = off; i < len+off; i++) {
			write(buf[i]);
		}
	}

	// 立即刷新流并向流中写入数据（不足以构成一个字节的数据自动补0）
	public void flush() throws IOException {
		if (pos_next==0) {
			bos.flush();
		} else {
			for (int i = 0; i < 8 - pos_next; i++) {
				byte_cur<<=1;
			}
			bos.write(byte_cur);
			bos.flush();
			pos_next=0;
			byte_cur=0;
		}
	}
	
	// 关闭流
	public void close() throws IOException {
		flush();
		bos.close();
		pos_next=-1;
		byte_cur=0;
	}
}

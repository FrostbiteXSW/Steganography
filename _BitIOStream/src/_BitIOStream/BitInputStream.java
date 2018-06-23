package _BitIOStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream {
	BufferedInputStream bis;	// ������
	private byte byte_cur=0;	// ��ǰ�����е��ֽ�
	private int pos_next=8;		// ��һ�������λ��
	
	// ���캯����ʹ��File��
	public BitInputStream(String fileName) throws IOException {
		bis=new BufferedInputStream(new FileInputStream(fileName));
	}
	
	// ���캯����ʹ��File��
	public BitInputStream(File file) throws IOException {
		bis=new BufferedInputStream(new FileInputStream(file));
	}

	// ���캯����ʹ��InputStream��
	public BitInputStream(InputStream inputStream) {
		bis=new BufferedInputStream(inputStream);
	}

	// �����ж�ȡһλ����
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
	
	// �����ж�ȡһ�����ݱ����������У�����ʵ�ʶ�ȡ��λ��
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
	
	// ��ȡָ�����ȵ�λ����������ʼƫ�������Ŀռ��ڣ�����ʵ�ʶ�ȡ��λ��
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
	
	// �������н������ļ�λ������ʵ��������λ��
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
	
	// ��ȡ����ʣ��ɶ�λ��
	public long available() throws IOException {
		return (bis.available()+1)*8-pos_next;
	}
	
	// �ر���
	public void close() throws IOException {
		bis.close();
		byte_cur=0;
		pos_next=8;
	}
}

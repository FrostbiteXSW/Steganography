package _Steganography;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import _BitIOStream.BitInputStream;
import _BitIOStream.BitOutputStream;

public class PNGReader extends ImageReader {
	// ���ι��캯��
	public PNGReader(int level,Mode mode) throws Exception {
		setLevel(level);
		setMode(mode);
	}

	@Override
	protected void steganography(String outPut) throws IOException {
		BufferedImage srcImg = ImageIO.read(srcIMG);
	    BitInputStream bitInputStream=new BitInputStream(srcFile);
	    
		// ���sizeֵ
		size=0;
		
		// ��������ļ�
		File file=new File(outPut);
		if (file.exists()) {
			if (file.isFile())
				file.delete();
		}
		
		int ret=-1;		// �����������ֵ
		
		switch (level) {
		case 1:
			ret=steganography_L1(srcImg,bitInputStream,file);
			break;
		case 2:
			ret=steganography_L2(srcImg, bitInputStream, file);
			break;
		case 3:
			ret=steganography_L3(srcImg, bitInputStream, file);
			break;
		}
		
		// ������ɣ��ر�������
		bitInputStream.close();
		
		// �ж��Ƿ�ɹ�д��
		if (ret==-1) {
			System.out.println("ͼƬ��С���㣬д��ʧ�ܡ�");
			size=0;
			file.delete();
		}
		else
			System.out.println("д����ɣ��ܸ�д��С��   "+(size/=8)+" Byte");
	}

	// ��д������ȵȼ�1��
	private int steganography_L1(BufferedImage srcImg,BitInputStream bitInputStream,File outPut) throws IOException {
	    int width = srcImg.getWidth(), height = srcImg.getHeight(), lenthremain = 64;
	    if (bitInputStream.available()+64>(long)width*(long)height*3)
			return -1;
		
	    int[] lenth=byte2bit(long2byte(bitInputStream.available()/8));
	    for (int w = 0; w < width; w++) {
	      for (int h = 0; h < height; h++) {
	        Color color = new Color(srcImg.getRGB(w, h));
	        if (lenthremain>=3) {
	        	color=new Color((lenth[64-(lenthremain--)]==1?color.getRed()|0x01:color.getRed()&0xFE),
								(lenth[64-(lenthremain--)]==1?color.getGreen()|0x01:color.getGreen()&0xFE), 
								(lenth[64-(lenthremain--)]==1?color.getBlue()|0x01:color.getBlue()&0xFE));
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=3;
			} else if (lenthremain==1) {
				color=new Color((lenth[64-(lenthremain--)]==1?color.getRed()|0x01:color.getRed()&0xFE),
								color.getGreen(), 
								color.getBlue());
				srcImg.setRGB(w, h, color.getRGB());
	        	size++;
			} else if (bitInputStream.available()>=3) {
				color=new Color((bitInputStream.read()==1?color.getRed()|0x01:color.getRed()&0xFE),
								(bitInputStream.read()==1?color.getGreen()|0x01:color.getGreen()&0xFE), 
								(bitInputStream.read()==1?color.getBlue()|0x01:color.getBlue()&0xFE));
				srcImg.setRGB(w, h, color.getRGB());
				size+=3;
	        } else if (bitInputStream.available()==2) {
	        	color=new Color((bitInputStream.read()==1?color.getRed()|0x01:color.getRed()&0xFE),
								(bitInputStream.read()==1?color.getGreen()|0x01:color.getGreen()&0xFE), 
								color.getBlue());
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=2;
			} else if (bitInputStream.available()==1) {
				color=new Color((bitInputStream.read()==1?color.getRed()|0x01:color.getRed()&0xFE),
								color.getGreen(), 
								color.getBlue());
				srcImg.setRGB(w, h, color.getRGB());
				size++;
			}
	      }
	    }
	    ImageIO.write(srcImg, "PNG", outPut);
		return 0;
	}

	// ��д������ȵȼ�2��
	private int steganography_L2(BufferedImage srcImg,BitInputStream bitInputStream,File outPut) throws IOException {
	    int width = srcImg.getWidth(), height = srcImg.getHeight(), lenthremain = 64;
	    if (bitInputStream.available()+64>(long)width*(long)height*3)
			return -1;
		
	    int[] lenth=byte2bit(long2byte(bitInputStream.available()/8));
	    for (int w = 0; w < width; w++) {
	      for (int h = 0; h < height; h++) {
	        Color color = new Color(srcImg.getRGB(w, h));
	        if (lenthremain>=6) {
	        	color=new Color((color.getRed()&0xFC)|(lenth[64-(lenthremain--)]*2+lenth[64-(lenthremain--)]), 
	        					(color.getGreen()&0xFC)|(lenth[64-(lenthremain--)]*2+lenth[64-(lenthremain--)]), 
	        					(color.getBlue()&0xFC)|(lenth[64-(lenthremain--)]*2+lenth[64-(lenthremain--)]));
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=6;
			} else if (lenthremain==4) {
				color=new Color((color.getRed()&0xFC)|(lenth[64-(lenthremain--)]*2+lenth[64-(lenthremain--)]), 
    							(color.getGreen()&0xFC)|(lenth[64-(lenthremain--)]*2+lenth[64-(lenthremain--)]), 
    							color.getBlue());
				srcImg.setRGB(w, h, color.getRGB());
	        	size+=4;
			} else if (bitInputStream.available()>=6) {
	        	color=new Color((color.getRed()&0xFC)|(bitInputStream.read()*2+bitInputStream.read()), 
    							(color.getGreen()&0xFC)|(bitInputStream.read()*2+bitInputStream.read()), 
    							(color.getBlue()&0xFC)|(bitInputStream.read()*2+bitInputStream.read()));
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=6;
	        } else if (bitInputStream.available()==4) {
	        	color=new Color((color.getRed()&0xFC)|(bitInputStream.read()*2+bitInputStream.read()), 
								(color.getGreen()&0xFC)|(bitInputStream.read()*2+bitInputStream.read()), 
								color.getBlue());
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=4;
			} else if (bitInputStream.available()==2) {
				color=new Color((color.getRed()&0xFC)|(bitInputStream.read()*2+bitInputStream.read()), 
								color.getGreen(), 
								color.getBlue());
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=2;
			}
	      }
	    }
	    ImageIO.write(srcImg, "PNG", outPut);
		return 0;
	}
	
	// ��д������ȵȼ�3��
	private int steganography_L3(BufferedImage srcImg,BitInputStream bitInputStream,File outPut) throws IOException {
	    int width = srcImg.getWidth(), height = srcImg.getHeight(), lenthremain = 64;
	    if (bitInputStream.available()+64>(long)width*(long)height*3)
			return -1;

	    int[] lenth=byte2bit(long2byte(bitInputStream.available()/8));
	    for (int w = 0; w < width; w++) {
	      for (int h = 0; h < height; h++) {
	        Color color = new Color(srcImg.getRGB(w, h));
	        if (lenthremain>=12) {
	        	color=new Color((color.getRed()&0xF0)|(lenth[64-(lenthremain--)]*8+lenth[64-(lenthremain--)]*4+lenth[64-(lenthremain--)]*2+lenth[64-(lenthremain--)]), 
	        					(color.getGreen()&0xF0)|(lenth[64-(lenthremain--)]*8+lenth[64-(lenthremain--)]*4+lenth[64-(lenthremain--)]*2+lenth[64-(lenthremain--)]), 
	        					(color.getBlue()&0xF0)|(lenth[64-(lenthremain--)]*8+lenth[64-(lenthremain--)]*4+lenth[64-(lenthremain--)]*2+lenth[64-(lenthremain--)]));
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=12;
			} else if (lenthremain==4) {
				color=new Color((color.getRed()&0xF0)|(lenth[64-(lenthremain--)]*8+lenth[64-(lenthremain--)]*4+lenth[64-(lenthremain--)]*2+lenth[64-(lenthremain--)]), 
    							color.getGreen(),
    							color.getBlue());
				srcImg.setRGB(w, h, color.getRGB());
	        	size+=4;
			} else if (bitInputStream.available()>=12) {
	        	color=new Color((color.getRed()&0xF0)|(bitInputStream.read()*8+bitInputStream.read()*4+bitInputStream.read()*2+bitInputStream.read()), 
    							(color.getGreen()&0xF0)|(bitInputStream.read()*8+bitInputStream.read()*4+bitInputStream.read()*2+bitInputStream.read()),
    							(color.getBlue()&0xF0)|(bitInputStream.read()*8+bitInputStream.read()*4+bitInputStream.read()*2+bitInputStream.read()));
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=12;
	        } else if (bitInputStream.available()==8) {
	        	color=new Color((color.getRed()&0xF0)|(bitInputStream.read()*8+bitInputStream.read()*4+bitInputStream.read()*2+bitInputStream.read()), 
								(color.getGreen()&0xF0)|(bitInputStream.read()*8+bitInputStream.read()*4+bitInputStream.read()*2+bitInputStream.read()),
								color.getBlue());
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=8;
			} else if (bitInputStream.available()==4) {
				color=new Color((color.getRed()&0xF0)|(bitInputStream.read()*8+bitInputStream.read()*4+bitInputStream.read()*2+bitInputStream.read()), 
								color.getGreen(),
								color.getBlue());
	        	srcImg.setRGB(w, h, color.getRGB());
	        	size+=4;
			}
	      }
	    }
	    ImageIO.write(srcImg, "PNG", outPut);
		return 0;
	}
	
	@Override
	protected void unSteganography(String outPut) throws IOException {
		// ���sizeֵ
		size=0;
		
		// ��������ļ�
		File file=new File(outPut);
		if (file.exists()) {
			if (file.isFile())
				file.delete();
		}
		file.createNewFile();
		
		// ����ͼƬ�����ļ���
		BufferedImage srcImg = ImageIO.read(srcIMG);
		BitOutputStream bitOutputStream=new BitOutputStream(outPut);
		
		int ret=-1;		// �����������ֵ
		
		// ������ȵȼ�ѡ���㷨
		switch (level) {
		case 1:
			ret=unSteganography_L1(srcImg, bitOutputStream);
			break;
		case 2:
			ret=unSteganography_L2(srcImg, bitOutputStream);
			break;
		case 3:
			ret=unSteganography_L3(srcImg, bitOutputStream);
			break;
		}
		
		// ������ɣ��ر�������
		bitOutputStream.close();
		
		if (ret==-1) { // δ����ȷ�����ļ�
			System.out.println("�����ļ�ʧ�ܡ�");
			file.delete();
		} else // �����ļ����
			System.out.println("�����ļ���ɣ��ܴ�С��   "+(size/=8)+" Byte");
	}

	// ������д���ļ�����ȵȼ�1��
	private int unSteganography_L1(BufferedImage srcImg, BitOutputStream bitOutputStream) throws IOException {
	    int width = srcImg.getWidth(), height = srcImg.getHeight(), lenthremain = 64;
	    int[] bitstream=new int[64];
	    long lenth=-1;
	    for (int w = 0; w < width; w++) {
	      for (int h = 0; h < height; h++) {
	        Color color = new Color(srcImg.getRGB(w, h));
	        int RMsg=color.getRed()&0x01,
	        	GMsg=color.getGreen()&0x01,
	        	BMsg=color.getBlue()&0x01;
	        if (lenthremain>=3) {
				bitstream[64-(lenthremain--)]=RMsg;
				bitstream[64-(lenthremain--)]=GMsg;
				bitstream[64-(lenthremain--)]=BMsg;
	        } else if (lenthremain==1) {
	        	bitstream[64-(lenthremain--)]=RMsg;
	        	lenth=byte2long(bit2byte(bitstream))*8;
			} else if (lenth>=3) {
				bitOutputStream.write(new int[]{RMsg,GMsg,BMsg});
				size+=3;
				lenth-=3;
			} else if (lenth==2) {
				bitOutputStream.write(new int[]{RMsg,GMsg});
				size+=2;
				lenth-=2;
			} else if (lenth==1) {
				bitOutputStream.write(RMsg);
				size++;
				lenth--;
			} else if (lenthremain==0 && lenth==0)
				return 0;
			else
				return -1;
	      }
	    }
		return -1;
	}
	
	// ������д���ļ�����ȵȼ�2��
	private int unSteganography_L2(BufferedImage srcImg, BitOutputStream bitOutputStream) throws IOException {
	    int width = srcImg.getWidth(), height = srcImg.getHeight(), lenthremain = 64;
	    int[] bitstream=new int[64];
	    long lenth=-1;
	    for (int w = 0; w < width; w++) {
	      for (int h = 0; h < height; h++) {
	        Color color = new Color(srcImg.getRGB(w, h));
	        int RMsg=color.getRed()&0x03,
	        	GMsg=color.getGreen()&0x03,
	        	BMsg=color.getBlue()&0x03;
	        if (lenthremain>=6) {
	        	for (int Msg : new int[] {RMsg,GMsg,BMsg}) {
	        		bitstream[64-(lenthremain--)]=Msg&0x02;
					bitstream[64-(lenthremain--)]=Msg&0x01;
				}
	        } else if (lenthremain==4) {
	        	for (int Msg : new int[] {RMsg,GMsg}) {
	        		bitstream[64-(lenthremain--)]=Msg&0x02;
					bitstream[64-(lenthremain--)]=Msg&0x01;
				}
	        	lenth=byte2long(bit2byte(bitstream))*8;
			} else if (lenth>=6) {
				for (int Msg : new int[] {RMsg,GMsg,BMsg})
	        		bitOutputStream.write(new int[] {Msg&0x02,Msg&0x01});
				size+=6;
				lenth-=6;
			} else if (lenth==4) {
				for (int Msg : new int[] {RMsg,GMsg})
	        		bitOutputStream.write(new int[] {Msg&0x02,Msg&0x01});
				size+=4;
				lenth-=4;
			} else if (lenth==2) {
				bitOutputStream.write(new int[]{RMsg&0x02,RMsg&0x01});
				size+=2;
				lenth-=2;
			} else if (lenthremain==0 && lenth==0)
				return 0;
			else
				return -1;
	      }
	    }
		return -1;
	}
	
	// ������д���ļ�����ȵȼ�3��
	private int unSteganography_L3(BufferedImage srcImg, BitOutputStream bitOutputStream) throws IOException {
	    int width = srcImg.getWidth(), height = srcImg.getHeight(), lenthremain = 64;
	    int[] bitstream=new int[64];
	    long lenth=-1;
	    for (int w = 0; w < width; w++) {
	      for (int h = 0; h < height; h++) {
	        Color color = new Color(srcImg.getRGB(w, h));
	        int RMsg=color.getRed()&0x0F,
	        	GMsg=color.getGreen()&0x0F,
	        	BMsg=color.getBlue()&0x0F;
	        if (lenthremain>=12) {
	        	for (int Msg : new int[] {RMsg,GMsg,BMsg}) {
	        		bitstream[64-(lenthremain--)]=Msg&0x08;
					bitstream[64-(lenthremain--)]=Msg&0x04;
					bitstream[64-(lenthremain--)]=Msg&0x02;
					bitstream[64-(lenthremain--)]=Msg&0x01;
				}
	        } else if (lenthremain==4) {
	        	for (int Msg : new int[] {RMsg}) {
	        		bitstream[64-(lenthremain--)]=Msg&0x08;
					bitstream[64-(lenthremain--)]=Msg&0x04;
					bitstream[64-(lenthremain--)]=Msg&0x02;
					bitstream[64-(lenthremain--)]=Msg&0x01;
				}
	        	lenth=byte2long(bit2byte(bitstream))*8;
			} else if (lenth>=12) {
				for (int Msg : new int[] {RMsg,GMsg,BMsg})
	        		bitOutputStream.write(new int[] {Msg&0x08,Msg&0x04,Msg&0x02,Msg&0x01});
				size+=12;
				lenth-=12;
			} else if (lenth==8) {
				for (int Msg : new int[] {RMsg,GMsg})
					bitOutputStream.write(new int[] {Msg&0x08,Msg&0x04,Msg&0x02,Msg&0x01});
				size+=8;
				lenth-=8;
			} else if (lenth==4) {
				bitOutputStream.write(new int[] {RMsg&0x08,RMsg&0x04,RMsg&0x02,RMsg&0x01});
				size+=4;
				lenth-=4;
			} else if (lenthremain==0 && lenth==0)
				return 0;
			else
				return -1;
	      }
	    }
		return -1;
	}
}

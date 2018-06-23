package _Steganography;

import java.io.*;

public class BMPReader extends ImageReader {
	// ���ι��캯��
	public BMPReader(int level,Mode mode) throws Exception {
		setLevel(level);
		setMode(mode);
	}
	
	// ��ȡͼƬ������Ϣ��������ά���飨�кţ��кţ�RGB��
	private int[][][] readBMP(File src) throws IOException {
        FileInputStream fisBMP=new FileInputStream(src);			// ԭʼͼƬ��
        BufferedInputStream bisBMP=new BufferedInputStream(fisBMP);

        // �����ļ�ͷ��Ϣ
        bisBMP.skip(18);

        // ��ȡ��������
        byte[] b1=new byte[4];
        bisBMP.read(b1);
        byte[] b2=new byte[4];
        bisBMP.read(b2);

        int Width=byte2int(b1);
        int Height=byte2int(b2);

        // ��ΪBMPλͼ�Ķ�ȡ˳��Ϊ����ɨ�裬����Ӧ�����鶨��Ϊint[Height][Width]
        int[][][] data=new int[Height][Width][3];
        int skipnum=0;

        // BMPͼ������Ĵ�С����Ϊ4�ı����������������ֽڴ�һ�����أ�����ʱ��Ӧ���������ϵ�0
        if(Width*3%4!=0)
            skipnum=4-Width*3%4;
        
        bisBMP.skip(28);
        
        for(int i=0;i<data.length;i++) {
            for(int j=0;j<data[i].length;j++) {
                data[i][j][0]=bisBMP.read();		//R
                data[i][j][1]=bisBMP.read();		//G
                data[i][j][2]=bisBMP.read();		//B
            }
            if(skipnum!=0)
                bisBMP.skip(skipnum);
        }
        
        bisBMP.close();
        return data;
    }
	
	// ������д���ļ����ļ������磩��������ָ���ļ�·��
	protected void unSteganography(String outPut) throws IOException {
		int[][][] data=readBMP(srcIMG);	// ��ȡͼƬ������Ϣ
		
		// ���sizeֵ
        size=0;
        
		// ��������ļ�
        File file=new File(outPut);
        if (file.exists()) {
        	if (file.isFile())
				file.delete();
        }
        file.createNewFile();
        
        // ��������ļ���
        BufferedOutputStream bosFile=new BufferedOutputStream(new FileOutputStream(file));	// ���ͼƬ��
		int ret=-1;		// �����������ֵ
		
		// ������ȵȼ�ѡ���㷨
		switch (level) {
		case 1:
			ret=unSteganography_L1(data, bosFile);
			break;
		case 2:
			ret=unSteganography_L2(data, bosFile);
			break;
		case 3:
			ret=unSteganography_L3(data, bosFile);
			break;
		}
		
		// δ����ȷ�����ļ�
		if (ret==-1) {
			System.out.println("�����ļ�ʧ�ܡ�");
			bosFile.close();
			file.delete();
			return;
		}
		
		// �����ļ����
		System.out.println("�����ļ���ɣ��ܴ�С��   "+size+" Byte");
		bosFile.close();
		return;
	}
	
	// ������д���ļ����ļ������磬��ȵȼ�1�����ɹ�����0��ʧ�ܷ���-1
	private int unSteganography_L1(int[][][] data,BufferedOutputStream bosFile) throws IOException {
		// ��ȡͼƬ��������
		byte buf=0,			// ��ŵ�ǰ�ֽ�����
			 temp=0;		// ���dataת��Ϊbyte����ʱ����
        int count=0,		// ��ŵ�ǰ�ֽ�ʣ����Ҫд��λ��
        	times=8;		// ��ų����ֽ�ʣ���ȡλ��
        byte[] lenthbuf=new byte[8];
        					// �����ֽڻ���
        long lenth=0;		// �ļ�����
        boolean EOL=false;	// ��ȡ�ļ����Ƚ�����־
        
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < 3; k++) {
					temp=(byte)((data[i][j][k]&0xFF)&0x01);
					
					if (!EOL) { // ��ȡ�ļ�����
						if (times!=0) {
							lenthbuf[times-1]=(byte)((lenthbuf[times-1]>>1)&0x7F);
							lenthbuf[times-1]|=temp<<7;
							if (++count==8) {
								times--;
								count=0;
							}
						} else {
							lenth=byte2long(lenthbuf);
							buf=(byte)((buf>>1)&0x7F);
							buf|=temp<<7;
							count++;
							EOL=true;
						}
					} else if (lenth>0) { // ����ʣ�೤�ȶ�ȡ��Ӧ�������ֽ�
						buf=(byte)((buf>>1)&0x7F);
						buf|=temp<<7;
						if (++count==8) {
							bosFile.write(buf);
							size++;
							lenth--;
							count=0;
						}
					} else // ��ȡ���
						return 0;
				}
			}
		}
		
		// δ����ȷ�����ļ�
		return -1;
	}
	
	// ������д���ļ����ļ������磬��ȵȼ�2�����ɹ�����0��ʧ�ܷ���-1
	private int unSteganography_L2(int[][][] data,BufferedOutputStream bosFile) throws IOException {
		// ��ȡͼƬ��������
		byte buf=0,			// ��ŵ�ǰ�ֽ�����
			 temp=0;		// ���dataת��Ϊbyte����ʱ����
        int count=0,		// ��ŵ�ǰ�ֽ�ʣ����Ҫд��λ��
        	times=8;		// ��ų����ֽ�ʣ���ȡλ��
        byte[] lenthbuf=new byte[8];
        					// �����ֽڻ���
        long lenth=0;		// �ļ�����
        boolean EOL=false;	// ��ȡ�ļ����Ƚ�����־
        
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < 3; k++) {
					temp=(byte)((data[i][j][k]&0xFF)&0x03);
					
					if (!EOL) { // ��ȡ�ļ�����
						if (times!=0) {
							lenthbuf[times-1]=(byte)((lenthbuf[times-1]>>2)&0x3F);
							lenthbuf[times-1]|=temp<<6;
							if (++count==4) {
								times--;
								count=0;
							}
						} else {
							lenth=byte2long(lenthbuf);
							buf=(byte)((buf>>2)&0x3F);
							buf|=temp<<6;
							count++;
							EOL=true;
						}
					} else if (lenth>0) { // ����ʣ�೤�ȶ�ȡ��Ӧ�������ֽ�
						buf=(byte)((buf>>2)&0x3F);
						buf|=temp<<6;
						if (++count==4) {
							bosFile.write(buf);
							size++;
							lenth--;
							count=0;
						}
					} else // ��ȡ���
						return 0;
				}
			}
		}
		
		// δ����ȷ�����ļ�
		return -1;
	}
	
	// ������д���ļ����ļ������磬��ȵȼ�3�����ɹ�����0��ʧ�ܷ���-1
	private int unSteganography_L3(int[][][] data,BufferedOutputStream bosFile) throws IOException {
		// ��ȡͼƬ��������
		byte buf=0,			// ��ŵ�ǰ�ֽ�����
			 temp=0;		// ���dataת��Ϊbyte����ʱ����
        int count=0,		// ��ŵ�ǰ�ֽ�ʣ����Ҫд��λ��
        	times=8;		// ��ų����ֽ�ʣ���ȡλ��
        byte[] lenthbuf=new byte[8];
        					// �����ֽڻ���
        long lenth=0;		// �ļ�����
        boolean EOL=false;	// ��ȡ�ļ����Ƚ�����־
        
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < 3; k++) {
					temp=(byte)((data[i][j][k]&0xFF)&0x0F);
					
					if (!EOL) { // ��ȡ�ļ�����
						if (times!=0) {
							lenthbuf[times-1]=(byte)((lenthbuf[times-1]>>4)&0x0F);
							lenthbuf[times-1]|=temp<<4;
							if (++count==2) {
								times--;
								count=0;
							}
						} else {
							lenth=byte2long(lenthbuf);
							buf=(byte)((buf>>4)&0x0F);
							buf|=temp<<4;
							count++;
							EOL=true;
						}
					} else if (lenth>0) { // ����ʣ�೤�ȶ�ȡ��Ӧ�������ֽ�
						buf=(byte)((buf>>4)&0x0F);
						buf|=temp<<4;
						if (++count==2) {
							bosFile.write(buf);
							size++;
							lenth--;
							count=0;
						}
					} else // ��ȡ���
						return 0;
				}
			}
		}
		
		// δ����ȷ�����ļ�
		return -1;
	}
	
	// ��д�����ļ������磩����ָ���ļ�д��ָ��ͼƬ��������Ϊ��ͼƬ�ļ�
	protected void steganography(String outPut) throws IOException {
		BufferedInputStream bisBMP=new BufferedInputStream(new FileInputStream(srcIMG)),	// ԭʼͼƬ��
							bisFile=new BufferedInputStream(new FileInputStream(srcFile));	// ԭʼ�ļ���

		// ���sizeֵ
		size=0;
		
		// ��������ļ�
		File file=new File(outPut);
		if (file.exists()) {
			if (file.isFile())
				file.delete();
		}
		file.createNewFile();
		
		BufferedOutputStream bosBMP=new BufferedOutputStream(new FileOutputStream(file));	// ���ͼƬ��
		int ret=-1;		// �����������ֵ
		
		switch (level) {
			case 1:
				ret=steganography_L1(bisBMP, bisFile, bosBMP);
				break;
			case 2:
				ret=steganography_L2(bisBMP, bisFile, bosBMP);
				break;
			case 3:
				ret=steganography_L3(bisBMP, bisFile, bosBMP);
				break;
		}
		
		// ������ɣ��ر�������
		bisBMP.close();
		bisFile.close();
		bosBMP.close();
		
		// �ж��Ƿ�ɹ�д��
		if (ret==-1) {
			System.out.println("ͼƬ��С���㣬д��ʧ�ܡ�");
			size=0;
			file.delete();
		}
		else
			System.out.println("д����ɣ��ܸ�д��С��   "+(size/=8)+" Byte");
	}

	// ��д�����ļ������磬��ȵȼ�1�����ɹ�����0��ʧ�ܷ���-1
	private int steganography_L1(BufferedInputStream bisBMP,BufferedInputStream bisFile,BufferedOutputStream bosBMP) throws IOException {
		// �����ļ�ͷ��Ϣ
        for (int i = 0; i < 18; i++)
			bosBMP.write(bisBMP.read());
        
        // ��ȡ��������������
        byte[] b1=new byte[4];
        bisBMP.read(b1);
        bosBMP.write(b1);
        byte[] b2=new byte[4];
        bisBMP.read(b2);
        bosBMP.write(b2);

        int Width=byte2int(b1);
        int Height=byte2int(b2);
        
        // ͼƬ��С���㣬����д��
        if (((long)Width*(long)Height*3)<(srcFile.length()*8+64))
			return -1;
        
        // ����������0��λ��
        int skipnum=0;
        if(Width*3%4!=0)
            skipnum=4-Width*3%4;
        
        // �����ļ���Ϣ
        for (int i = 0; i < 28; i++)
			bosBMP.write(bisBMP.read());
        
        // ��������д�ļ�
        byte buf=0;			// ��ŵ�ǰ�ֽ�����
        int count=0;		// ��ŵ�ǰ�ֽ�ʣ����Ҫд��λ��
        byte[] lenth=long2byte(srcFile.length());	// ���д��ĳ�����Ϣ
        int times=8;		// ���lenthʣ��д�������Ϣ
        boolean EOF=false,	// �ж��Ƿ�����ļ�β��
        		EOL=false;	// �жϳ�����Ϣ�Ƿ�д��
        
        for(int i=0;i<Height;i++) { // �ļ��ܴ�С=��*��*RGB����Ϊѭ���ܴ�������Ϊ�ܹ���д���ļ���λ��
        	for(int j=0;j<Width*3;j++) {
        		// �жϳ�����Ϣ�Ƿ�д��
        		if (!EOL) {
        			// �ж���һ�ζ����8λ�����Ƿ�����д��ϣ����д�����ٴ�lenth�ж�ȡ8λ����
            		if(count==0) {
                		// �ж��Ƿ����lenthβ��
                		if(times==0) {
                			EOL=true;
                			
                			// ���ļ����ж�����
                			int temp=bisFile.read();	// ��ȡ�ļ�������һ���ֽ�
                    		
                    		// �ж��Ƿ�����ļ�β��
                    		if(temp==-1) {
                    			EOF=true;
                    			bosBMP.write(bisBMP.read());
    	    	        		continue;
                    		}
                    		count=8;	// �Ѷ�ȡ�ļ����еĺ�8λ����
                    		buf=(byte)(temp&0xFF);	// ����Ϊ�޷�����
                		}
                		else {
							buf=lenth[--times];
							count=8;
						}
                	}
            		
            		// ȡ��buf�����ݵĵ�λ���滻ԭʼͼƬ���¸��ֽڵĵ�λ��Ȼ��д�����ͼƬ���У�ͬʱbuf����������λ����
	        		bosBMP.write((bisBMP.read()&0xFE)|(buf&0x01));
	        		size++;buf>>>=1;count--;
				}
        		// �ж��ļ��Ƿ���д�꣬δд���������д
        		else if (!EOF) {
        			// �ж���һ�ζ����8λ�����Ƿ�����д��ϣ����д�����ٴ��ļ����ж�ȡ8λ����
            		if(count==0) {
                		int temp=bisFile.read();	// ��ȡ�ļ�������һ���ֽ�
                		
                		// �ж��Ƿ�����ļ�β��
                		if(temp==-1) {
                			EOF=true;
                			bosBMP.write(bisBMP.read());
	    	        		continue;
                		}
                		count=8;	// �Ѷ�ȡ�ļ����еĺ�8λ����
                		buf=(byte)(temp&0xFF);	// ����Ϊ�޷�����
                	}
            		
            		// ȡ��buf�����ݵĵ�λ���滻ԭʼͼƬ���¸��ֽڵĵ�λ��Ȼ��д�����ͼƬ���У�ͬʱbuf����������λ����
	        		bosBMP.write((bisBMP.read()&0xFE)|(buf&0x01));
	        		size++;buf>>>=1;count--;
	        	}
	    		// д����������дֱ�ӿ���
	        	else
	        		bosBMP.write(bisBMP.read());
        	}
    		// ֱ�ӿ���������0��Ϣ
            if(skipnum!=0) {
            	for (int j = 0; j < skipnum; j++)
        			bosBMP.write(bisBMP.read());
            }
        }
        
        return 0;
	}

	// ��д�����ļ������磬��ȵȼ�2�����ɹ�����0��ʧ�ܷ���-1
	private int steganography_L2(BufferedInputStream bisBMP,BufferedInputStream bisFile,BufferedOutputStream bosBMP) throws IOException {
		// �����ļ�ͷ��Ϣ
        for (int i = 0; i < 18; i++)
			bosBMP.write(bisBMP.read());
        
        // ��ȡ��������������
        byte[] b1=new byte[4];
        bisBMP.read(b1);
        bosBMP.write(b1);
        byte[] b2=new byte[4];
        bisBMP.read(b2);
        bosBMP.write(b2);

        int Width=byte2int(b1);
        int Height=byte2int(b2);
        
        // ͼƬ��С���㣬����д��
        if (((long)Width*(long)Height*3)<(srcFile.length()*4+32))
			return -1;
        
        // ����������0��λ��
        int skipnum=0;
        if(Width*3%4!=0)
            skipnum=4-Width*3%4;
        
        // �����ļ���Ϣ
        for (int i = 0; i < 28; i++)
			bosBMP.write(bisBMP.read());
        
        // ��������д�ļ�
        byte buf=0;			// ��ŵ�ǰ�ֽ�����
        int count=0;		// ��ŵ�ǰ�ֽ�ʣ����Ҫд��λ��
        byte[] lenth=long2byte(srcFile.length());	// ���д��ĳ�����Ϣ
        int times=8;		// ���lenthʣ��д�������Ϣ
        boolean EOF=false,	// �ж��Ƿ�����ļ�β��
        		EOL=false;	// �жϳ�����Ϣ�Ƿ�д��
        
        for(int i=0;i<Height;i++) { // �ļ��ܴ�С=��*��*RGB����Ϊѭ���ܴ�������Ϊ�ܹ���д���ļ���λ��
        	for(int j=0;j<Width*3;j++) {
        		// �жϳ�����Ϣ�Ƿ�д��
        		if (!EOL) {
        			// �ж���һ�ζ����8λ�����Ƿ�����д��ϣ����д�����ٴ�lenth�ж�ȡ8λ����
            		if(count==0) {
                		// �ж��Ƿ����lenthβ��
                		if(times==0) {
                			EOL=true;
                			
                			// ���ļ����ж�����
                			int temp=bisFile.read();	// ��ȡ�ļ�������һ���ֽ�
                    		
                    		// �ж��Ƿ�����ļ�β��
                    		if(temp==-1) {
                    			EOF=true;
                    			bosBMP.write(bisBMP.read());
    	    	        		continue;
                    		}
                    		count=8;	// �Ѷ�ȡ�ļ����еĺ�8λ����
                    		buf=(byte)(temp&0xFF);	// ����Ϊ�޷�����
                		}
                		else {
							buf=lenth[--times];
							count=8;
						}
                	}
            		
            		// ȡ��buf�����ݵĵ�λ���滻ԭʼͼƬ���¸��ֽڵĵ�λ��Ȼ��д�����ͼƬ���У�ͬʱbuf����������λ����
	        		bosBMP.write((bisBMP.read()&0xFC)|(buf&0x03));
	        		size+=2;buf>>>=2;count-=2;
				}
        		// �ж��ļ��Ƿ���д�꣬δд���������д
        		else if (!EOF) {
        			// �ж���һ�ζ����8λ�����Ƿ�����д��ϣ����д�����ٴ��ļ����ж�ȡ8λ����
            		if(count==0) {
                		int temp=bisFile.read();	// ��ȡ�ļ�������һ���ֽ�
                		
                		// �ж��Ƿ�����ļ�β��
                		if(temp==-1) {
                			EOF=true;
                			bosBMP.write(bisBMP.read());
	    	        		continue;
                		}
                		count=8;	// �Ѷ�ȡ�ļ����еĺ�8λ����
                		buf=(byte)(temp&0xFF);	// ����Ϊ�޷�����
                	}
            		
            		// ȡ��buf�����ݵĵ�λ���滻ԭʼͼƬ���¸��ֽڵĵ�λ��Ȼ��д�����ͼƬ���У�ͬʱbuf����������λ����
	        		bosBMP.write((bisBMP.read()&0xFC)|(buf&0x03));
	        		size+=2;buf>>>=2;count-=2;
	        	}
	    		// д����������дֱ�ӿ���
	        	else
	        		bosBMP.write(bisBMP.read());
        	}
    		// ֱ�ӿ���������0��Ϣ
            if(skipnum!=0) {
            	for (int j = 0; j < skipnum; j++)
        			bosBMP.write(bisBMP.read());
            }
        }
        
        return 0;
	}

	// ��д�����ļ������磬��ȵȼ�3�����ɹ�����0��ʧ�ܷ���-1
	private int steganography_L3(BufferedInputStream bisBMP,BufferedInputStream bisFile,BufferedOutputStream bosBMP) throws IOException {
		// �����ļ�ͷ��Ϣ
        for (int i = 0; i < 18; i++)
			bosBMP.write(bisBMP.read());
        
        // ��ȡ��������������
        byte[] b1=new byte[4];
        bisBMP.read(b1);
        bosBMP.write(b1);
        byte[] b2=new byte[4];
        bisBMP.read(b2);
        bosBMP.write(b2);

        int Width=byte2int(b1);
        int Height=byte2int(b2);
        
        // ͼƬ��С���㣬����д��
        if (((long)Width*(long)Height*3)<(srcFile.length()*2+16))
			return -1;
		
        // ����������0��λ��
        int skipnum=0;
        if(Width*3%4!=0)
            skipnum=4-Width*3%4;
        
        // �����ļ���Ϣ
        for (int i = 0; i < 28; i++)
			bosBMP.write(bisBMP.read());
        
        // ��������д�ļ�
        byte buf=0;			// ��ŵ�ǰ�ֽ�����
        int count=0;		// ��ŵ�ǰ�ֽ�ʣ����Ҫд��λ��
        byte[] lenth=long2byte(srcFile.length());	// ���д��ĳ�����Ϣ
        int times=8;		// ���lenthʣ��д�������Ϣ
        boolean EOF=false,	// �ж��Ƿ�����ļ�β��
        		EOL=false;	// �жϳ�����Ϣ�Ƿ�д��
        
        for(int i=0;i<Height;i++) { // �ļ��ܴ�С=��*��*RGB����Ϊѭ���ܴ�������Ϊ�ܹ���д���ļ���λ��
        	for(int j=0;j<Width*3;j++) {
        		// �жϳ�����Ϣ�Ƿ�д��
        		if (!EOL) {
        			// �ж���һ�ζ����8λ�����Ƿ�����д��ϣ����д�����ٴ�lenth�ж�ȡ8λ����
            		if(count==0) {
                		// �ж��Ƿ����lenthβ��
                		if(times==0) {
                			EOL=true;
                			
                			// ���ļ����ж�����
                			int temp=bisFile.read();	// ��ȡ�ļ�������һ���ֽ�
                    		
                    		// �ж��Ƿ�����ļ�β��
                    		if(temp==-1) {
                    			EOF=true;
                    			bosBMP.write(bisBMP.read());
    	    	        		continue;
                    		}
                    		count=8;	// �Ѷ�ȡ�ļ����еĺ�8λ����
                    		buf=(byte)(temp&0xFF);	// ����Ϊ�޷�����
                		}
                		else {
							buf=lenth[--times];
							count=8;
						}
                	}
            		
            		// ȡ��buf�����ݵĵ�λ���滻ԭʼͼƬ���¸��ֽڵĵ�λ��Ȼ��д�����ͼƬ���У�ͬʱbuf����������λ����
	        		bosBMP.write((bisBMP.read()&0xF0)|(buf&0x0F));
	        		size+=4;buf>>>=4;count-=4;
				}
        		// �ж��ļ��Ƿ���д�꣬δд���������д
        		else if (!EOF) {
        			// �ж���һ�ζ����8λ�����Ƿ�����д��ϣ����д�����ٴ��ļ����ж�ȡ8λ����
            		if(count==0) {
                		int temp=bisFile.read();	// ��ȡ�ļ�������һ���ֽ�
                		
                		// �ж��Ƿ�����ļ�β��
                		if(temp==-1) {
                			EOF=true;
                			bosBMP.write(bisBMP.read());
	    	        		continue;
                		}
                		count=8;	// �Ѷ�ȡ�ļ����еĺ�8λ����
                		buf=(byte)(temp&0xFF);	// ����Ϊ�޷�����
                	}
            		
            		// ȡ��buf�����ݵĵ�λ���滻ԭʼͼƬ���¸��ֽڵĵ�λ��Ȼ��д�����ͼƬ���У�ͬʱbuf����������λ����
	        		bosBMP.write((bisBMP.read()&0xF0)|(buf&0x0F));
	        		size+=4;buf>>>=4;count-=4;
	        	}
	    		// д����������дֱ�ӿ���
	        	else
	        		bosBMP.write(bisBMP.read());
        	}
    		// ֱ�ӿ���������0��Ϣ
            if(skipnum!=0) {
            	for (int j = 0; j < skipnum; j++)
        			bosBMP.write(bisBMP.read());
            }
        }
        
        return 0;
	}
}

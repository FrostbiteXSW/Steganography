package _Steganography;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import _BitIOStream.BitInputStream;
import _Steganography.JPGReader.HuffmanTable.HuffmanTableType;

public class JPGReader extends ImageReader {
	public static void main(String[] args) {
		JPGReader jpgReader;
		try {
			jpgReader = new JPGReader(1,Mode.Steganography);
			showHuffmanTableInfo("test.jpg");
			ArrayList<MCUMatrix> MCUMatrixs = jpgReader.decodeSOS("test.jpg");
			System.out.println("��������" + MCUMatrixs.size() + "�ű�");
			System.in.read();
			int count = 0;
			for (MCUMatrix MCUMatrix : MCUMatrixs) {
				System.out.println("����" + (++count));
				System.out.println("�����ͣ�" + MCUMatrix.type.toString());
				System.out.println("���������£�");
				for (int i = 0; i < 64; i++) {
					System.out.print(MCUMatrix.matrix[i] + " ");
					if (i % 8 == 7) {
						System.out.println();
					}
				}
				if (count % 20 == 0) {
					System.out.println("��������������");
					System.in.read();
				}
			}
		} catch (Exception e) {
			System.out.print("Exception:");
			e.printStackTrace();
			File file = new File("tempfile.tmp");
			if (file.exists())
				file.delete();
		}
	}
	
	// ���ι��캯��
	public JPGReader(int level,Mode mode) throws Exception {
		setLevel(level);
		setMode(mode);
	}
	
	private ERRCODE LAST_ERROR = ERRCODE.NO_ERROR;	// ��һ�η����Ĵ�������
	
	// ������
	enum ERRCODE {
		NO_ERROR,		// �޴���
		FORMAT_ERROR,	// ��ʽ����
		RESOLVE_ERROR,	// ��������
	}
	
	// ��ȡ��һ�η����Ĵ�������
	public String getLastError() {
		return LAST_ERROR.toString();
	}
	
	// ������������Ҷ�ӽڵ�
	class HuffmanTableLeafPoint {
		int[] code;				// ����
		byte value = 0x00;		// Ȩֵ
	}
	
	// ����������
	class HuffmanTable {
		// ������������
		class HuffmanTableType {
			static final byte DC0 = 0x00,	// DCֱ��0�ű�
							  DC1 = 0x01,	// DCֱ��1�ű�
							  AC0 = 0x10,	// AC����0�ű�
							  AC1 = 0x11;	// AC����1�ű�	  
		}
		
		byte type;
		int length;
		HuffmanTableLeafPoint[] points;
	}
	
	// ��ʾJPG�ļ�����������Ϣ��������;��
	public static void showHuffmanTableInfo(String filePath) throws Exception {
		JPGReader jpgReader = new JPGReader(1, Mode.Steganography);
		ArrayList<HuffmanTable> huffmanTables = jpgReader.getHuffmanTable(filePath);
		System.out.println("�����ķ��ؽ����" + jpgReader.getLastError());
		if (jpgReader.getLastError() != ERRCODE.NO_ERROR.toString())
			return;
		System.out.println("�ܹ��������Ĺ�����������" + huffmanTables.size());
		int tableIndex = 0;
		for (HuffmanTable huffmanTable : huffmanTables) {
			System.out.println("\n��������" + (++tableIndex));
			System.out.print("�����ͣ�");
			switch (huffmanTable.type) {
			case HuffmanTableType.AC0:
				System.out.println("AC0");
				break;
			case HuffmanTableType.AC1:
				System.out.println("AC1");
				break;
			case HuffmanTableType.DC0:
				System.out.println("DC0");
				break;
			case HuffmanTableType.DC1:
				System.out.println("DC1");
				break;
			}
			System.out.println("�����ݣ�");
			int leafIndex = 1;
			for (HuffmanTableLeafPoint huffmanTableLeafPoint : huffmanTable.points) {
				String codeStr = "";
				for (int code : huffmanTableLeafPoint.code) {
					switch (code) {
					case 0:
						codeStr += '0';
						break;
					case 1:
						codeStr += '1';
						break;
					}
				}
				System.out.printf("%3d. %16s  0x%02x\n", leafIndex++, codeStr, huffmanTableLeafPoint.value);
			}
		}
	}
	
	// ��JPG�ļ��н�����������
	private ArrayList<HuffmanTable> getHuffmanTable(String filePath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		if (bis.read() != 0xFF || bis.read() != 0xD8) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return null;
		}
		ArrayList<HuffmanTable> huffmanTables = new ArrayList<>();	// ����������
		while (bis.available() > 0) {
			if (bis.read() == 0xFF) {
				int temp = bis.read();
				if (temp == 0xC4) {
					// ��ȡDHT�γ�
					int DHTLength = ((bis.read() << 8) | bis.read());
					
					// ��ȡDHT��
					byte[] DHT = new byte[DHTLength - 2];
					if (bis.read(DHT) != DHTLength - 2) {
						LAST_ERROR = ERRCODE.RESOLVE_ERROR;
						bis.close();
						return null;
					}
					
					// ����DHT��
					int pos = 0;	// ��һ����ȡ����λ�ñ��
					HuffmanTable huffmanTable = new HuffmanTable();				// ������������
					while (pos < DHT.length) {
						// ��ȡ������������
						huffmanTable.type = DHT[pos++];
						
						// ������Ҷ�ӽ������
						int codeCount = 0;
						for (int i = 0; i < 16; i++) {
							codeCount += DHT[pos + i] & 0xFF;
						}
						
						huffmanTable.points = new HuffmanTableLeafPoint[codeCount];	// ��ʼ��Ҷ�ӽڵ�
						int curLeafPoint = 0, 		// ��ǰ�����Ҷ�ӽڵ�
							curCodeLengthCount = 0; // ��ǰ���ȵ���������
						
						// �����������
						for (int curCodeLength = 1; curCodeLength <= 16; curCodeLength++) {
							// ��ȡ��ǰ���ȵ���������
							curCodeLengthCount = DHT[pos + curCodeLength - 1] & 0xFF;
							
							// ���ݵ�ǰ���ȵ������������ɶ�Ӧ������Ҷ�ӽڵ�
							for (int j = 0; j < curCodeLengthCount; j++) {
								huffmanTable.points[curLeafPoint] = new HuffmanTableLeafPoint();
								huffmanTable.points[curLeafPoint].code = new int[curCodeLength];
								for (int k = 0; k < curCodeLength; k++) {
									huffmanTable.points[curLeafPoint].code[k] = 0;
								}
								
								// �ǵ�һ������
								if (curLeafPoint > 0) {
									boolean plusFlag = false; // �Ѵ������ּ�һ��־
									for (int i = huffmanTable.points[curLeafPoint - 1].code.length - 1; i >= 0; i--) {
										if (!plusFlag) {
											if (huffmanTable.points[curLeafPoint - 1].code[i] == 0) {
												huffmanTable.points[curLeafPoint].code[i] = 1;
												plusFlag = true;
											} else {
												huffmanTable.points[curLeafPoint].code[i] = 0;
											}
										} else {
											huffmanTable.points[curLeafPoint].code[i] = huffmanTable.points[curLeafPoint - 1].code[i];
										}
									}
								}
								huffmanTable.points[curLeafPoint].value = DHT[pos + 16 + curLeafPoint];
								curLeafPoint++;
							}
						}
						
						// ���浱ǰ�����������λ�õ���һ����������������ڣ�
						huffmanTables.add(huffmanTable);
						huffmanTable = new HuffmanTable();
						pos = pos + codeCount + 16; 
					}
				} else if (temp == 0xDA) {
					break;
				}
			}
		}
		if (huffmanTables.isEmpty()) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return null;
		}
		bis.close();
		return huffmanTables;
	}
	
	// �������г̱������Ϊ��ֵ
	private static int decodeRLE(int[] code) {
		int decode = 0;
		if (code.length == 0) {
			return 0;
		}
		if (code[0] == 0) {
			for (int i = 0; i < code.length; i++) {
				if (code[i] != 0 && code[i] != 1) {
					return 0;
				} else {
					decode = (decode << 1) + Math.abs(code[i] - 1);
				}

			}
			decode = -decode;
		} else {
			for (int i = 0; i < code.length; i++) {
				if (code[i] != 0 && code[i] != 1) {
					return 0;
				} else {
					decode = (decode << 1) + code[i];
				}
			}
		}
		return decode;
	}
	
	// �������г̱������Ϊ��ֵ
		private static int decodeRLE(ArrayList<Integer> code) {
			int decode = 0;
			if (code.size() == 0) {
				return 0;
			}
			if (code.get(0) == 0) {
				for (int i = 0; i < code.size(); i++) {
					if (code.get(i) != 0 && code.get(i) != 1) {
						return 0;
					} else {
						decode = (decode << 1) + Math.abs(code.get(i) - 1);
					}

				}
				decode = -decode;
			} else {
				for (int i = 0; i < code.size(); i++) {
					if (code.get(i) != 0 && code.get(i) != 1) {
						return 0;
					} else {
						decode = (decode << 1) + code.get(i);
					}
				}
			}
			return decode;
		}
	
	// ��ӡJPG����������Ϣ��������;��
	public static void printPicMatrixInfo(String filePath) throws IOException {
		BitInputStream bis = new BitInputStream(filePath);
		int[] buf = new int[8];
		while (bis.available() > 0) {
			bis.read(buf);
			if (Arrays.equals(buf, new int[] {1,1,1,1,1,1,1,1})) {
				bis.read(buf);
				if (Arrays.equals(buf, new int[] {1,1,0,1,1,0,1,0})) {
					int temp = 0;
					for (int i = 0; i < 16; i++) {
						temp = (temp << 1) | bis.read();
					}
					bis.skip((temp - 2) * 8);
					temp = 0;
					for (int i = 0; i < 320; i++) {
						System.out.print(bis.read());
						if ((++temp) % 4 == 0) {
							System.out.print(" ");
							if (temp % 32 == 0) {
								System.out.println();
							}
						}
					}
					return;
				}
			}
		}
	}
	
	// ����JPG����
	private ArrayList<MCUMatrix> decodeSOS(String filePath) throws IOException {
		// �ж��Ƿ�֧�ִ�JPGͼƬ�������ȸ�ʽ
		switch (getSOF0Accuracy(filePath)) {
		case YCrCb411:
			break;
		case YCrCb111:
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			return null;
		default:
			return null;
		}
		
		// ��ȡ��������
		ArrayList<HuffmanTable> huffmanTables = null;
		if ((huffmanTables = getHuffmanTable(filePath)) == null)
			return null;
		
		// ��ȡSOS������Ϣ
		MCUTableSetting[] MCUTableSettings = getMCUTableSettings(filePath);
		
		// ��ȡRST�����Ϣ
		int RSTValue = getRST(filePath);
		if (RSTValue == -1)
			return null;
		
		// ��ת�����ݶ�
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
		while (bufferedInputStream.available() > 0) {
			if (bufferedInputStream.read() == 0xFF) {
				if (bufferedInputStream.read() == 0xDA) {
					bufferedInputStream.skip(12);
					break;
				}
			}
		}
		
		// ��ȡ���ݶβ��滻�����־
		ArrayList<Byte> buf = new ArrayList<>();
		while (bufferedInputStream.available() > 0) {
			buf.add((byte) bufferedInputStream.read());
			if ((buf.get(buf.size() - 1) & 0xFF) == 0xFF) {
				int temp = bufferedInputStream.read();
				while ((byte) temp == 0xFF) 
					// ����0xFFFF������ǰһ��FF���Ժ�һ�������ж�
					temp = bufferedInputStream.read();
				switch (temp) {
					case 0x00:
						// ����0xFF00�����б����滻
						break;
					case 0xD9:
						// ����0xFFD9�����ݶν���
						buf.remove(buf.size() - 1);
						bufferedInputStream.skip(bufferedInputStream.available());
						break;
					default:
						if (temp >= 0xD0 && temp <= 0xD7)
							// ����0xFFD0~D7��ΪRST��ǣ�����
							buf.remove(buf.size() - 1);
						else
							// ����0xFF??������FF����������ֵ��������
							buf.remove(buf.size() - 1);
							buf.add((byte) temp);
						break;
				}
			}
		}
		bufferedInputStream.close();
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("tempfile.tmp"));
		for (Byte bytedata : buf) 
			bos.write(bytedata);
		bos.close();
		
		// �������ݶ�
		BitInputStream bis = new BitInputStream("tempfile.tmp");
		ArrayList<MCUMatrix> MCUMatrixs = new ArrayList<>();
		int YCount = 0, curMatrixPos = 0;
		MCUMatrix matrix = new MCUMatrix();
		matrix.type = MCUType.Y;
		ArrayList<Integer> codeCount = new ArrayList<>();
		HuffmanTable usingDCTable = null, usingACTable = null;
		for (MCUTableSetting setting : MCUTableSettings) {
			if (setting.type == matrix.type) {
				for (HuffmanTable huffmanTable : huffmanTables) {
					if (huffmanTable.type == setting.DCTable)
						usingDCTable = huffmanTable;
					else if (huffmanTable.type == setting.ACTable)
						usingACTable = huffmanTable;
				}
				break;
			}
		}
		while (bis.available() > 0) {
			boolean notFind = true;
			while (notFind) {
				codeCount.add(bis.read());
				if (codeCount.size() >= 32) {
					LAST_ERROR = ERRCODE.RESOLVE_ERROR;
					bis.close();
					File file = new File("tempfile.tmp");
					if (file.exists())
						file.delete();
					return null;
				}
				if (curMatrixPos == 0) {
					for (HuffmanTableLeafPoint point : usingDCTable.points) {
						if (point.code.length == codeCount.size()) {
							notFind = false;
							for (int i = 0; i < codeCount.size(); i++) {
								if (point.code[i] != codeCount.get(i)) {
									notFind = true;
									break;
								}
							}
							if (!notFind) {
								codeCount = new ArrayList<>();
								for (int i = 0; i < point.value; i++)
									codeCount.add(bis.read());
								matrix.matrix[curMatrixPos++] = decodeRLE(codeCount);
								codeCount = new ArrayList<>();
								break;
							}
						} else if (point.code.length > codeCount.size())
							break;
					}
				} else {
					for (HuffmanTableLeafPoint point : usingACTable.points) {
						if (point.code.length == codeCount.size()) {
							notFind = false;
							for (int i = 0; i < codeCount.size(); i++) {
								if (point.code[i] != codeCount.get(i)) {
									notFind = true;
									break;
								}
							}
							if (!notFind) {
								for (int i = 0; i < ((point.value >> 4) & 0x0F); i++) 
									matrix.matrix[curMatrixPos++] = 0;		// �˴������ʣ�Ϊʲô�γ����0��������ᳬ������Ĵ�С64��������Ҫ��ô�죿
								if ((point.value & 0x0F) == 0) {
									for (int i = curMatrixPos; i < 64; i++)
										matrix.matrix[i] = 0;
									curMatrixPos = 0;
									codeCount = new ArrayList<>();
								} else {
									codeCount = new ArrayList<>();
									for (int i = 0; i < point.value; i++)
										codeCount.add(bis.read());
									matrix.matrix[curMatrixPos++] = decodeRLE(codeCount);
									codeCount = new ArrayList<>();
									if (curMatrixPos == 64)
										curMatrixPos = 0;
								}
								break;
							}
						} else if (point.code.length > codeCount.size())
							break;
					}
				}
			}
			if (curMatrixPos == 0) {
				MCUMatrixs.add(matrix);
				if (matrix.type == MCUType.Y) {
					matrix = new MCUMatrix();
					if (++YCount == 4) {
						matrix.type = MCUType.Cr;
						usingDCTable = null;
						usingACTable = null;
						for (MCUTableSetting setting : MCUTableSettings) {
							if (setting.type == matrix.type) {
								for (HuffmanTable huffmanTable : huffmanTables) {
									if (huffmanTable.type == setting.DCTable)
										usingDCTable = huffmanTable;
									else if (huffmanTable.type == setting.ACTable)
										usingACTable = huffmanTable;
								}
								break;
							}
						}
					} else
						matrix.type = MCUType.Y;
				} else if (matrix.type == MCUType.Cr) {
					matrix = new MCUMatrix();
					YCount = 0;
					matrix.type = MCUType.Cb;
					usingDCTable = null;
					usingACTable = null;
					for (MCUTableSetting setting : MCUTableSettings) {
						if (setting.type == matrix.type) {
							for (HuffmanTable huffmanTable : huffmanTables) {
								if (huffmanTable.type == setting.DCTable)
									usingDCTable = huffmanTable;
								else if (huffmanTable.type == setting.ACTable)
									usingACTable = huffmanTable;
							}
							break;
						}
					}
				} else if (matrix.type == MCUType.Cb) {
					matrix = new MCUMatrix();
					matrix.type = MCUType.Y;
					usingDCTable = null;
					usingACTable = null;
					for (MCUTableSetting setting : MCUTableSettings) {
						if (setting.type == matrix.type) {
							for (HuffmanTable huffmanTable : huffmanTables) {
								if (huffmanTable.type == setting.DCTable)
									usingDCTable = huffmanTable;
								else if (huffmanTable.type == setting.ACTable)
									usingACTable = huffmanTable;
							}
							break;
						}
					}
				}
			}
		}
		File file = new File("tempfile.tmp");
		if (file.exists())
			file.delete();
		bis.close();
		return MCUMatrixs;
	}

	// ��ȡDRI�����й�RST����Ϣ
	private int getRST(String filePath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		if (bis.read() != 0xFF || bis.read() != 0xD8) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return -1;
		}
		while (bis.available() > 0) {
			if (bis.read() == 0xFF) {
				int temp = bis.read();
				if (temp == 0xDD) {
					// ��ת��RST�����Ϣλ��
					bis.skip(2);
					
					// ����RST�����Ϣ
					temp = (bis.read() << 8) | bis.read();
					bis.close();
					return temp;
				} else if (temp == 0xDA) {
					bis.close();
					return 0;
				}
			}
		}
		LAST_ERROR = ERRCODE.RESOLVE_ERROR;
		bis.close();
		return -1;
	}
	
	// MCU��Ԫ��������
	enum MCUType {
		Y,
		Cr,
		Cb
	}
	
	// ��������
	enum Accuracy {
		YCrCb411,
		YCrCb111
	}
	
	// ����MCU8x8����
	class MCUMatrix {
		MCUType type;
		int[] matrix = new int[64];
	}
	
	// MCU����ʹ�õĹ���������
	class MCUTableSetting {
		MCUType type;
		byte DCTable, ACTable;
	}
	
	// ��ȡSOF0�����йز������ȵ���Ϣ
	private Accuracy getSOF0Accuracy(String filePath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		if (bis.read() != 0xFF || bis.read() != 0xD8) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return null;
		}
		while (bis.available() > 0) {
			if (bis.read() == 0xFF) {
				if (bis.read() == 0xC0) {
					// ��ת������������Ϣλ��
					bis.skip(9);
					
					// ��������������Ϣ
					switch (bis.read()) {
					case 0x22:
						bis.close();
						return Accuracy.YCrCb411;
					case 0x11:
						bis.close();
						return Accuracy.YCrCb111;
					default:
						LAST_ERROR = ERRCODE.RESOLVE_ERROR;
						bis.close();
						return null;
					}
				}
			}
		}
		LAST_ERROR = ERRCODE.RESOLVE_ERROR;
		bis.close();
		return null;
	}
	
	// ��ȡSOS�����й���ɫ��������ʹ�õĹ�������ŵ���Ϣ
	private MCUTableSetting[] getMCUTableSettings(String filePath) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
		if (bis.read() != 0xFF || bis.read() != 0xD8) {
			LAST_ERROR = ERRCODE.FORMAT_ERROR;
			bis.close();
			return null;
		}
		MCUTableSetting[] MCUTableSettings = new MCUTableSetting[3];	// ����������
		while (bis.available() > 0) {
			if (bis.read() == 0xFF) {
				if (bis.read() == 0xDA) {
					// ��ת����ɫ������Ϣλ��
					bis.skip(3);
					
					// ������ɫ������Ϣ
					for (int i = 0; i < 3; i++) {
						MCUTableSettings[i] = new MCUTableSetting();
						
						// ��ȡ��������
						switch (bis.read()) {
						case 1:
							MCUTableSettings[i].type = MCUType.Y;
							break;
						case 2:
							MCUTableSettings[i].type = MCUType.Cr;
							break;
						case 3:
							MCUTableSettings[i].type = MCUType.Cb;
							break;
						default:
							LAST_ERROR = ERRCODE.RESOLVE_ERROR;
							bis.close();
							return null;
						}
						
						// ��ȡ��ʹ�õ�ֱ����źͽ������
						byte temp = (byte) bis.read(), DC = (byte) ((temp >> 4) & 0x0F), AC = (byte) (temp & 0x0F);
						switch (DC & 0xFF) {
						case 0:
							MCUTableSettings[i].DCTable = HuffmanTableType.DC0;
							break;
						case 1:
							MCUTableSettings[i].DCTable = HuffmanTableType.DC1;
							break;
						default:
							LAST_ERROR = ERRCODE.RESOLVE_ERROR;
							bis.close();
							return null;
						}
						switch (AC & 0xFF) {
						case 0:
							MCUTableSettings[i].ACTable = HuffmanTableType.AC0;
							break;
						case 1:
							MCUTableSettings[i].ACTable = HuffmanTableType.AC1;
							break;
						default:
							LAST_ERROR = ERRCODE.RESOLVE_ERROR;
							bis.close();
							return null;
						}
					}
				}
			}
		}
		bis.close();
		return MCUTableSettings;
	}
		
	@Override
	protected void steganography(String outPut) throws IOException {
		// TODO �Զ����ɵķ������

	}

	@Override
	protected void unSteganography(String outPut) throws IOException {
		// TODO �Զ����ɵķ������

	}
}
package cz.afri.smg.types;

public class CType {

  private static final int SIZE_INT = 4;

  public static CType createTypeWithLength(final int pSizeInBytes) {
		return new CType(pSizeInBytes);
	}

	public static CType getIntType() {
		return createTypeWithLength(SIZE_INT);
	}

  public static CType unknownType() {
		// TODO Auto-generated method stub
		return null;
	}

  private final int size;

	public CType(final int pSize) {
    size = pSize;
  }

	public final int getSize() {
		return size;
	}

}

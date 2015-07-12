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

	@Override
	public String toString() {
	  return size + "b type";
	}

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + size;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CType other = (CType) obj;
    if (size != other.size)
      return false;
    return true;
  }
}

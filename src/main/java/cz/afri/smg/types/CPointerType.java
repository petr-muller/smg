package cz.afri.smg.types;


public class CPointerType extends CType {

	private static final int POINTER_SIZE = 8;

  public CPointerType() {
    super(POINTER_SIZE);
  }

  public static CType getVoidPointer() {
		return new CPointerType();
	}

}

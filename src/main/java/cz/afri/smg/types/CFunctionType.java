package cz.afri.smg.types;

public final class CFunctionType {

  private final CType returnType;

  private CFunctionType(final CType pReturnType) {
    returnType = pReturnType;
  }

  public CType getReturnType() {
		return returnType;
	}

	public static CFunctionType createSimpleFunctionType(final CType pReturnType) {
	  return new CFunctionType(pReturnType);
	}

}

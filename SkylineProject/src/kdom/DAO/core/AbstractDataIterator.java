package kdom.DAO.core;


public abstract class AbstractDataIterator implements DataIterator{

	public AbstractDataIterator() {
	
	}
	
	@Deprecated
	public void remove(){
		throw new UnsupportedOperationException();
	}

}

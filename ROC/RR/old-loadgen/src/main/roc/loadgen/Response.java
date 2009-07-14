package roc.loadgen;

import java.io.Serializable;

public interface Response extends Serializable {

    public boolean isOK();
    public boolean isError();

    public Throwable getThrowable();

}

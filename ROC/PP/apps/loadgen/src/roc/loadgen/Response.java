package roc.loadgen;

import java.io.Serializable;

public interface Response extends Serializable {

    public boolean isOK();
    public boolean isError();
    public long getRespTime();
    public void setRespTime(long respTime);
    public Throwable getThrowable();

}

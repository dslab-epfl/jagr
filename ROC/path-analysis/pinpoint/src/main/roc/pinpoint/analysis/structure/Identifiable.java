package roc.pinpoint.analysis.structure;

import java.util.Map;

public interface Identifiable {
    public boolean matchesId( Map attrs );
    public Map getId();
}

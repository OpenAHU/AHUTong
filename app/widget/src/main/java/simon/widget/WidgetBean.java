package simon.widget;

import java.io.Serializable;

public class WidgetBean implements Serializable {
    public String name;
    public String times;
    public int type;
    public boolean isFinish;
    public WidgetBean(String name, String time, int type, boolean isFinish){
        this.name=name;
        this.times=time;
        this.type=type;
        this.isFinish=isFinish;
    }
}

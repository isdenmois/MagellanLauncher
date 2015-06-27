package isden.mois.magellanlauncher;

/**
 * Created by isden on 20.06.15.
 */


public class Application {
    public boolean inner;
    public String name;
    public String packageName;
    public Class activity;

    public Application() {
    }

    public Application(String packageName, String name) {
        inner = false;
        this.name = name;
        this.packageName = packageName;
    }

    public Application(Class activity, String name) {
        this.inner = true;
        this.activity = activity;
        this.name = name;
    }
}

class AppComparator implements java.util.Comparator<Application> {
    @Override
    public int compare(Application a, Application b) {
        if ((a == null || a.name == null) && (b == null || b.name == null)) {
            return 0;
        }
        if ((a == null || a.name == null) && (b != null && b.name != null)) {
            return 1;
        }
        if ((a != null && a.name != null) && (b == null || b.name == null)) {
            return -1;
        }

        return a.name.compareToIgnoreCase(b.name);
    }
}
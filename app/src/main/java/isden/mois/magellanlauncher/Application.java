package isden.mois.magellanlauncher;

/**
 * Created by isden on 20.06.15.
 */


class Application {
    String name;
    String packageName;
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
package ademar.textlauncher;

final class Model {

    final long id;
    final String label;
    final String labelSearch;
    final String packageName;

    Model(long id, String label, String packageName) {
        this.id = id;
        this.label = label;
        this.labelSearch = label.toLowerCase();
        this.packageName = packageName;
    }

}

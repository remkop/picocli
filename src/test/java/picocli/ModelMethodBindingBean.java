package picocli;

class ModelMethodBindingBean {
    private int x = 7;

    private int getX() {
        return x;
    }

    private void setX(int x) {
        this.x = x;
    }

    public int publicGetX() {
        return x;
    }
}

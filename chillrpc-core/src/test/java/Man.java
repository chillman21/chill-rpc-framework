import lombok.Builder;

/**
 * @author NIU
 * @createTime 2020/7/21 15:41
 */
@Builder
public class Man {
    private String name;

    Man(String name) {
        this.name = name;
    }

    public static Man.ManBuilder builder() {
        return new Man.ManBuilder();
    }

    public static class ManBuilder {
        private String name;

        ManBuilder() {
        }

        public Man.ManBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Man build() {
            return new Man(this.name);
        }

        public String toString() {
            return "Man.ManBuilder(name=" + this.name + ")";
        }
    }
}

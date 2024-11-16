package ru.otus.chat.entities;

public class Role {
    private final Long id;
    private final String roleName;

    private Role(RoleBuilder builder) {
        this.id = builder.id;
        this.roleName = builder.roleName;
    }

    public Long getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public static class RoleBuilder {
        private Long id;
        private String roleName;

        public RoleBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public RoleBuilder setRoleName(String roleName) {
            this.roleName = roleName;
            return this;
        }

        public Role build() {
            return new Role(this);
        }
    }
}

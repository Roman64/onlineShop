package net.thumbtack.onlineshop.data.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import java.util.Objects;

@Entity
public class Admin extends User{

    @Column(nullable = true)
    private String position;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Admin admin = (Admin) o;
        return position.equals(admin.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position);
    }
}

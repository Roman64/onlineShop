package net.thumbtack.onlineshop.data.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;


@Entity
public class Client extends User {

    @Column(nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    @NotNull
    private String address;

    @Column(nullable = false)
    @NotNull
    private String phone;

    @Column(nullable = false)
    @NotNull
    private Integer deposit = 0;

    @OneToOne (cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn (name="cartWithProducts_id", referencedColumnName = "id")
    private CartWithProducts cartWithProducts;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getDeposit() {
        return deposit;
    }

    public void setDeposit(Integer deposit) {
        this.deposit = deposit;
    }

    public CartWithProducts getCartWithProducts() {
        return cartWithProducts;
    }

    public void setCartWithProducts(CartWithProducts cartWithProducts) {
        this.cartWithProducts = cartWithProducts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Client client = (Client) o;
        return email.equals(client.email) &&
                address.equals(client.address) &&
                phone.equals(client.phone) &&
                deposit.equals(client.deposit) &&
                Objects.equals(cartWithProducts, client.cartWithProducts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), email, address, phone, deposit);
    }

    //    @Override
//    public String toString() {
//        return "Client{" +
//                "email='" + email + '\'' +
//                ", address='" + address + '\'' +
//                ", phoneNumber='" + phone + '\'' +
//                ", cash=" + deposit +
//                ", cartWithProducts=" + cartWithProducts +
//                '}';
//    }
}

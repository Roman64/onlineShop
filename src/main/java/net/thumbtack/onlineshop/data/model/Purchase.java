package net.thumbtack.onlineshop.data.model;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class Purchase implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "cartWithProducts_id")
    private CartWithProducts cartWithProducts;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column
    private Integer quantity;



    public Integer getId() {
        return id;
    }

    public CartWithProducts getCartWithProducts() {
        return cartWithProducts;
    }

    public void setCartWithProducts(CartWithProducts cartWithProducts) {
        this.cartWithProducts = cartWithProducts;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Purchase purchase = (Purchase) o;
        return id.equals(purchase.id) &&
                quantity.equals(purchase.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity);
    }
}

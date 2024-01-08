package com.dao;

import java.util.List;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.dto.Admin;
import com.dto.Cart;
import com.dto.Item;
import com.dto.Product;
import com.dto.User;

public class ShoppingDao {
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("prem");
	EntityManager em = emf.createEntityManager();
	EntityTransaction et = em.getTransaction();
	Scanner sc = new Scanner(System.in);

	public void CartCheckOut() {
		System.out.print("Enter the Cart Id to checkout :  ");
		Cart cart = findCart(sc.nextInt());
		System.out.println("Total Price = " + cart.getCartTotal());
		System.out.println("Pay Amount : ");
		double cashAccepted = sc.nextDouble();

		while (cashAccepted < cart.getCartTotal()) {
			System.err.println("<-------Pay The Whole Amount------>");
			cashAccepted = sc.nextDouble();
		}

		double returnableCash = cashAccepted - cart.getCartTotal();
		if (returnableCash > 0) {

			System.out.println("Take the Balance Amount : " + returnableCash);
			System.out.println("Thank You For the Shopping...#)");
		} else {
			System.out.println("Thank You For the Shopping...#)");
		}

	}

	public Item findItemByName(String name) {
		Query q = em.createQuery("select i from Item i where i.itemName=?1");
		q.setParameter(1, name);
		return (Item) q.getSingleResult();
	}

	public Product removeProductFromCart() {
		System.out.print("Entet the Cart Id : ");
		Cart cart = findCart(sc.nextInt());
		System.out.print("Enter the Product Id : ");
		Product product = findProduct(sc.nextInt());
		if (cart != null) {
			List<Product> products = cart.getProducts();
			if (products.contains(product)) {
				products.remove(product);
				cart.setProducts(products);
				removeProduct(product);

				Item item = findItemByName(product.getProductName());
				item.setItemQuantity(item.getItemQuantity() + product.getProductQuantity());
				updateItem(item, item.getItemId());

				double totalprice = 0;
				for (Product p : products) {
					totalprice += (p.getProductQuantity() * p.getProductPrice());
				}
				cart.setCartTotal(totalprice);
				updateCart(cart, cart.getCartId());
				return product;
			} else {
				System.out.println("Product not present with given id");
				return null;
			}
		} else {
			System.out.println("Cart not present with given id");
			return null;
		}

	}

	public Product removeProduct(Product product) {
		et.begin();
		em.remove(product);
		et.commit();
		return product;
	}

	public Product findProduct(int ProductId) {
		return em.find(Product.class, ProductId);
	}

	public Product saveProduct(Product product) {
		et.begin();
		em.persist(product);
		et.commit();
		return product;
	}

	public Product updateProduct(Product product, int productId) {
		Product exprod = findProduct(productId);
		if (exprod != null) {
			product.setProductId(productId);
			et.begin();
			em.merge(product);
			et.commit();
			return product;
		}
		System.out.println("Product is not found !!!");
		return null;

	}

	public Cart addProductToCart() {
		System.out.print("Enter the CardId : ");
		Cart c = findCart(sc.nextInt());
		if (c != null) {
			System.out.print("Enter the Item Id :");
			Item i = findItem(sc.nextInt());

			// Convert item into object to product object
			Product product = new Product();
			product.setProductName(i.getItemName());
			product.setProductDescription(i.getItemDescription());
			product.setProductPrice(i.getItemPrice());
			System.out.print("Enter how much quantity you want : ");
			int quantity = sc.nextInt();
			product.setProductQuantity(quantity);

			// Save Product
			saveProduct(product);

			i.setItemQuantity(i.getItemQuantity() - quantity);
			updateItem(i, i.getItemId());

			// Assign this product to cart and update the cart
			List<Product> products = c.getProducts();
			products.add(product);
			c.setProducts(products);

			double totalprice = 0;
			for (Product p : products) {
				totalprice += (p.getProductQuantity() * p.getProductPrice());
			}

			c.setCartTotal(totalprice);

			return updateCart(c, c.getCartId());
		} else {
			return null;
		}
	}

	public Item updateItem(Item item, int itemId) {
		item.setItemId(itemId);
		et.begin();
		em.merge(item);
		et.commit();
		return item;
	}

	public Cart updateCart(Cart cart, int cartId) {
		Cart ex = findCart(cartId);
		if (ex != null) {
			cart.setCartId(cartId);
			et.begin();
			em.merge(cart);
			et.commit();
			return cart;
		}
		return null;
	}

	public Cart findCart(int cartId) {
		return em.find(Cart.class, cartId);
	}

	public User saveUser() {
		Scanner sc = new Scanner(System.in);
		User user = new User();

		System.out.print("Enter User Name : ");
		user.setUserName(sc.nextLine());
		System.out.print("Enter User Email : ");
		user.setUserEmail(sc.nextLine());
		System.out.print("Enter User Password : ");
		user.setUserPassword(sc.nextLine());
		System.out.print("Enter User Contact : ");
		user.setUserContact(sc.nextLong());
		user.setCart(new Cart());
		et.begin();
		em.persist(user);
		et.commit();
		return user;
	}

	public Item DeleteItemByName() {
		System.out.println("Enter an Admin id : ");
		Admin exadmin = findAdmin(sc.nextInt());
		Item i = findItemByName();

		if (exadmin != null) {
			if (exadmin.getItems().contains(i)) {
				List<Item> items = exadmin.getItems();
				items.remove(i);
				exadmin.setItems(items);
				return removeItem(i);
			} else {
				System.out.println("Item Does not exist or Item not present in th admin");
				return null;
			}
		} else {
			System.out.println("Admin does not exist with given id");
			return null;
		}
	}

	public Item findItemByName() {
		System.out.print("Enter the item name : ");
		Query q = em.createQuery("select i from Item i where i.itemName=?1");
		q.setParameter(1, sc.next());
		return (Item) q.getSingleResult();
	}

	public Item deleteItemFromAdmin() {
		System.out.println("Enter an admin id : ");
		Admin exadmin = findAdmin(sc.nextInt());
		System.out.println("Enter an Item id : ");
		Item i = findItem(sc.nextInt());
		if (exadmin != null) {
			if (exadmin.getItems().contains(i)) {
				List<Item> items = exadmin.getItems();
				items.remove(i);
				exadmin.setItems(items);
//				updateAdmin(exadmin, exadmin.getAdminId());
//				et.begin();
//				em.remove(i);
//				et.commit();
//				return i;
				return removeItem(i);
			} else {
				System.out.println("Item Does not exist or Item not present in th admin");
				return null;
			}
		} else {
			System.out.println("Admin does not exist with given id");
			return null;
		}
	}

	public Item findItem(int id) {
		return em.find(Item.class, id);
	}

	public Item removeItem(Item item) {
		et.begin();
		em.remove(item);
		et.commit();
		return item;
	}

	public Admin findAdmin(int adminId) {
		return em.find(Admin.class, adminId);
	}

	public Admin saveAdmin() {
		Scanner sc = new Scanner(System.in);
		Admin admin = new Admin();
		System.out.print("Enter the name : ");
		admin.setAdminName(sc.nextLine());
		System.out.print("Enter the Password  : ");
		admin.setAdiminPassword(sc.nextLine());
		System.out.print("Enter the Email : ");
		admin.setAdminEmail(sc.nextLine());
		System.out.print("Enter the Phone Number : ");
		admin.setPhone(sc.nextLong());
// List<Item> items = new ArrayList<Item>();
//		admin.setItems(new ArrayList<Item>());
		et.begin();
		em.persist(admin);
		et.commit();
		return admin;
	}

	public Product saveProduct(int itemId) {
		Item item = findItem(itemId);
		if (item != null) {
			Product prod = new Product();
			prod.setProductName(item.getItemName());
			prod.setProductDescription(item.getItemDescription());
			prod.setProductPrice(item.getItemPrice());
			prod.setProductQuantity(0);
			et.begin();
			em.persist(prod);
			et.commit();
			return prod;
		}
		return null;
	}

	public Item saveItem() {
		Scanner sc = new Scanner(System.in);
		Item item = new Item();
		System.out.print("Enter the name  of item : ");
		item.setItemName(sc.nextLine());
		System.out.print("Enter The Description : ");
		item.setItemDescription(sc.nextLine());
		System.out.print("Enter the Price : ");
		item.setItemPrice(sc.nextDouble());
		System.out.print("Enter the Quantity : ");
		item.setItemQuantity(sc.nextInt());

		et.begin();
		em.persist(item);
		et.commit();
		return item;
	}

	public Admin updateAdmin(Admin admin, int admimId) {
		Admin exAdmin = findAdmin(admimId);
		if (exAdmin != null) {
			admin.setAdminId(exAdmin.getAdminId());
			et.begin();
			em.merge(admin);
			et.commit();
			return admin;
		}
		return null;
	}

	public Item addItemToAdmin() {
		System.out.print("Enter the admin id : ");
		int adminId = sc.nextInt();
		Admin exadmin = findAdmin(adminId);
		List<Item> items = exadmin.getItems();
		Item i = saveItem();
		items.add(i);
		exadmin.setItems(items);
		updateAdmin(exadmin, adminId);
		return i;
	}
}
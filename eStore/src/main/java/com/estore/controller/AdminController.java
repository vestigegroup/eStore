package com.estore.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.estore.dao.ProductDao;
import com.estore.model.Product;

@Controller
public class AdminController {
	
	@Autowired
	private ProductDao productDao;
	
	private Path path;
	
	@RequestMapping("/admin")
	public String adminPage() {
		return "admin";
	}
	
	@RequestMapping("/admin/productInventory")
	public String productInventory(Model model) {
		List<Product> products = productDao.getAllProducts();
		model.addAttribute("products", products);
		
		return "productInventory";
	}
	
	@RequestMapping("/admin/productInventory/addProduct")
	public String addProduct(Model model) {
		Product product = new Product();
		product.setProductCategory("Instruments");
		product.setProductCondition("New");
		product.setProductStatus("Active");
		model.addAttribute("product", product);
		
		return "addProduct";
	}
	
	@RequestMapping(value = "/admin/productInventory/addProduct", method = RequestMethod.POST)
	public String addProductPost(@Valid @ModelAttribute("product") Product product, BindingResult result, HttpServletRequest request) {

		if(result.hasErrors()) {
			return "addProduct";
		}

		// Save the product data (except the product image) into the database.
		productDao.addProduct(product);
		
		// Save the product image in the resources/images folder.
		MultipartFile productImage = product.getProductImage();
		String rootDirectory = request.getSession().getServletContext().getRealPath("/");
		path = Paths.get(rootDirectory + "\\WEB-INF\\resources\\images\\" + product.getProductId() + ".png");
		if(null != productImage && !productImage.isEmpty()) {
			try {
				productImage.transferTo(new File(path.toString()));
			}
			catch (Exception e) {
				e.printStackTrace();
				
				throw new RuntimeException("Product image saving failed.", e);
			}
		}
		
		return "redirect:/admin/productInventory";
	}
	
	@RequestMapping("/admin/productInventory/deleteProduct/{productId}")
	public String deleteProduct(@PathVariable long productId, HttpServletRequest request) {
		// Delete the product data from the database.
		productDao.deleteProduct(productId);
		
		// Delete the product image from the resources/images folder.
		String rootDirectory = request.getSession().getServletContext().getRealPath("/");
		path = Paths.get(rootDirectory + "\\WEB-INF\\resources\\images\\" + productId + ".png");
		if(Files.exists(path)) {
			try {
				Files.delete(path);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "redirect:/admin/productInventory";
	}

	@RequestMapping("/admin/productInventory/editProduct/{productId}")
	public String editProduct(@PathVariable long productId, Model model) {
		Product product = productDao.getProductById(productId);
		model.addAttribute("product", product);
		
		return "editProduct";
	}
	
	@RequestMapping(value = "/admin/productInventory/editProduct", method = RequestMethod.POST)
	public String editProduct(@Valid @ModelAttribute("product") Product product, BindingResult result, Model model, HttpServletRequest request) {

		if(result.hasErrors()) {
			return "editProduct";
		}

		// Edit the product data (except the product image) into the database.
		productDao.editProduct(product);

		// Edit the product image in the resources/images folder.
		MultipartFile productImage = product.getProductImage();
		String rootDirectory = request.getSession().getServletContext().getRealPath("/");
		path = Paths.get(rootDirectory + "\\WEB-INF\\resources\\images\\" + product.getProductId() + ".png");
		if(null != productImage && !productImage.isEmpty()) {
			try {
				productImage.transferTo(new File(path.toString()));
			}
			catch (Exception e) {
				e.printStackTrace();
				
				throw new RuntimeException("Product image saving failed.", e);
			}
		}

		return "redirect:/admin/productInventory";
	}
	
}
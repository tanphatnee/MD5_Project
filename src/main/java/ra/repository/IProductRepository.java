package ra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.model.domain.Product;

import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {

    boolean existsByProductName(String productName);
    List<Product> findByCategoryId(Long categoryId);

}

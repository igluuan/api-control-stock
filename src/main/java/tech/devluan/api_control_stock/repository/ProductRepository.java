package tech.devluan.api_control_stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.devluan.api_control_stock.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
}

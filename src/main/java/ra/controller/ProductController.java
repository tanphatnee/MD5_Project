package ra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ra.model.dto.request.ProductRequest;
import ra.model.dto.response.ProductResponse;
import ra.service.impl.ProductService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/product")
@CrossOrigin("*")
public class ProductController {
    @Autowired
    private ProductService productService;
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return new  ResponseEntity<>(productService.findAll(), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id){
        return new  ResponseEntity<>(productService.findById(id), HttpStatus.OK);
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@ModelAttribute ProductRequest productRequest) {
        return new ResponseEntity<>(productService.save(productRequest),HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(@ModelAttribute ProductRequest productRequest,@PathVariable Long id){
        return new ResponseEntity<>(productService.update(productRequest,id),HttpStatus.CREATED);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ProductResponse> deleteById(@PathVariable Long id) {
        return new ResponseEntity<>(productService.delete(id), HttpStatus.OK);
    }
}

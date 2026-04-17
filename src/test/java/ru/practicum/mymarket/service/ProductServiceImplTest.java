package ru.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.mymarket.model.Product;
import ru.practicum.mymarket.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void loadProductsFromCsv_parsesAndFiltersCorrectly(@TempDir Path tempDir) throws IOException {
        String csv = """
                id,title,description,imgPath,price
                1,Widget, A nice widget , img/widget.jpg , 1990
                2,Old item,Already in DB,img/old.jpg,500
                ,Blank id,Blank id row,img/blank.jpg,100
                4,   ,Empty title,img/notitle.jpg,200
                5,Bad price,Non-numeric price,img/bad.jpg,abc
                6,Gadget,A cool gadget,,2500
                6,Gadget dup,Duplicate externalId,img/dup.jpg,2600
                """;
        Path csvFile = tempDir.resolve("products.csv");
        Files.writeString(csvFile, csv);

        when(productRepository.findAllExternalIds()).thenReturn(Set.of("2"));

        productService.loadProductsFromCsv(csvFile);

        ArgumentCaptor<List<Product>> captor = ArgumentCaptor.captor();
        verify(productRepository).saveAll(captor.capture());

        List<Product> saved = captor.getValue();
        assertThat(saved).hasSize(2);

        Product widget = saved.get(0);
        assertThat(widget.getTitle()).isEqualTo("Widget");
        assertThat(widget.getDescription()).isEqualTo("A nice widget");
        assertThat(widget.getImgPath()).isEqualTo("img/widget.jpg");
        assertThat(widget.getPrice()).isEqualTo(1990L);
        assertThat(widget.getExternalId()).isEqualTo("1");

        Product gadget = saved.get(1);
        assertThat(gadget.getTitle()).isEqualTo("Gadget");
        assertThat(gadget.getDescription()).isEqualTo("A cool gadget");
        assertThat(gadget.getImgPath()).isNull();
        assertThat(gadget.getPrice()).isEqualTo(2500L);
        assertThat(gadget.getExternalId()).isEqualTo("6");
    }

    @Test
    void loadProductsFromCsv_nonExistentFile_exitsNormally(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("nonexistent.csv");

        when(productRepository.findAllExternalIds()).thenReturn(Set.of());

        productService.loadProductsFromCsv(missing);

        verify(productRepository, never()).saveAll(any());
    }
}

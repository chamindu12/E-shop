package backend.controller;

import backend.exception.InventoryNotFoundException;
import backend.model.InventoryModel;
import backend.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ObjectMapper objectMapper;

    // Get the data using inventoryModel and send the data and save it to database through Repository
    @PostMapping("/inventory")
    public InventoryModel newInventoryModel(@RequestBody InventoryModel newInventoryModel) {
        logger.info("Creating new inventory item: {}", newInventoryModel.getItemName());
        InventoryModel savedItem = inventoryRepository.save(newInventoryModel);
        logger.info("Inventory item created with ID: {}", savedItem.getId());
        return savedItem;
    }

    // Insert the Images
    @PostMapping("/inventory/itemImg")
    public String itemImage(@RequestParam("file") MultipartFile file) {
        String folder = "src/main/uploads/";
        String itemImage = file.getOriginalFilename();
        logger.info("Uploading item image: {}", itemImage);

        try {
            File uploadDir = new File(folder);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            file.transferTo(Paths.get(folder + itemImage));
            logger.info("Image uploaded successfully: {}", itemImage);
        } catch (IOException e) {
            logger.error("Error uploading file: {}", itemImage, e);
            return "Error uploading file; " + itemImage;
        }
        return itemImage;
    }

    // Data display
    @GetMapping("/inventory")
    public List<InventoryModel> getAllItems() {
        logger.info("Fetching all inventory items");
        return inventoryRepository.findAll();
    }

    @GetMapping("/inventory/{id}")
    public InventoryModel getItemId(@PathVariable Long id) {
        logger.info("Fetching inventory item with ID: {}", id);
        return inventoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Inventory item not found with ID: {}", id);
                    return new InventoryNotFoundException(id);
                });
    }

    // We create a separate function for inserting the images. So we have to create a separate API for displaying images.
    private final String UPLOAD_DIR = "src/main/uploads/";

    @GetMapping("/uploads/{filename}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String filename) {
        logger.info("Fetching image: {}", filename);
        File file = new File(UPLOAD_DIR + filename);
        if (!file.exists()) {
            logger.warn("Image not found: {}", filename);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new FileSystemResource(file));
    }

    // Item update function
    @PutMapping("/inventory/{id}")
    public InventoryModel updateItem(
            @RequestPart(value = "itemDetails") String itemDetails,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @PathVariable Long id
    ) {
        logger.info("Updating inventory item with ID: {}", id);
        if (file != null) {
            logger.info("File received: {}", file.getOriginalFilename());
        } else {
            logger.info("No file uploaded");
        }

        InventoryModel newInventory;
        try {
            newInventory = objectMapper.readValue(itemDetails, InventoryModel.class);
        } catch (IOException e) {
            logger.error("Error parsing item details for ID: {}", id, e);
            throw new RuntimeException("Error parsing itemDetails", e);
        }

        return inventoryRepository.findById(id).map(existingInventory -> {
            existingInventory.setItemId(newInventory.getItemId());
            existingInventory.setItemName(newInventory.getItemName());
            existingInventory.setItemCategory(newInventory.getItemCategory());
            existingInventory.setItemQty(newInventory.getItemQty());
            existingInventory.setItemDetails(newInventory.getItemDetails());

            if (file != null && !file.isEmpty()) {
                String folder = "src/main/uploads/";
                String itemImage = file.getOriginalFilename();
                try {
                    file.transferTo(Paths.get(folder + itemImage));
                    existingInventory.setItemImage(itemImage);
                    logger.info("Image uploaded successfully for item ID: {}", id);
                } catch (IOException e) {
                    logger.error("Error saving image for item ID: {}", id, e);
                    throw new RuntimeException("Error saving image", e);
                }
            }
            logger.info("Inventory item updated successfully with ID: {}", id);
            return inventoryRepository.save(existingInventory);
        }).orElseThrow(() -> {
            logger.warn("Inventory item not found for update with ID: {}", id);
            return new InventoryNotFoundException(id);
        });
    }

    // Item delete
    @DeleteMapping("/inventory/{id}")
    public String deleteItem(@PathVariable Long id) {
        logger.info("Attempting to delete inventory item with ID: {}", id);
        InventoryModel inventoryItem = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(id));

        String itemImage = inventoryItem.getItemImage();
        if (itemImage != null && !itemImage.isEmpty()) {
            File imageFile = new File("src/main/uploads/" + itemImage);
            if (imageFile.exists()) {
                if (imageFile.delete()) {
                    logger.info("Image deleted successfully for item ID: {}", id);
                } else {
                    logger.warn("Image deletion failed for item ID: {}", id);
                }
            }
        }

        inventoryRepository.deleteById(id);
        logger.info("Inventory item deleted successfully with ID: {}", id);
        return "Data with id " + id + " deleted successfully";
    }
}

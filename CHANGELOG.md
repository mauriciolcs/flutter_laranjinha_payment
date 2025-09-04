## 1.0.0
* First release of the plugin.

* Implementação inicial com funcionalidades de pagamento, estorno e impressão.

## 1.0.1

* Printing adjustments:
    - Moved execution to a separate thread to avoid UI blocking
    - Automatically resized images to the printer's standard width
    - Ensured consistent sizing between text and images

## 1.0.2

* Printing improvements:
    - Optimized bitmap generation for faster performance
    - Reduced processing time when creating print content
    - Maintained consistency in text and image rendering

## 1.0.3

* Reduced `compileSdk` requirement to improve compatibility with older projects.
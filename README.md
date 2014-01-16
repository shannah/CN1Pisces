#Codename One 2D Graphics Library (Pisces)

This is a port of the [Pisces graphics library](https://code.google.com/p/pisces-graphics/) for use with [Codename One](http://www.codenameone.com) applications.  It provides a 2D drawing canvas that can be used for drawing arbitrary shapes and paths in Codename One.

##License

GPL with CLASSPATH exception

##Features

1. Draw shapes (ovals, rectangles, lines, polygons, and arbitrary paths).
2. Path drawing supports moveTo(), lineTo(), quadTo(), and cubicTo() methods.  This is all of the primitive drawing operations that should be necessary for doing all/most 2D drawing.
3. Affine Transforms
4. Draw images
5. Supports drawing strings, but this has not been tested yet and likely requires more work to port this over.
6. Supports all platforms that Codename One runs on.  Including (but not limited to) iOS, Android, Windows Phone, Blackberry, and J2ME.

##Installation

1. Copy the [CN1Pisces.cn1lib](https://github.com/shannah/CN1Pisces/raw/master/dist/CN1Pisces.cn1lib) file into your project's lib directory.
2. Refresh libs in your project.  In Netbeans this can be done by right-clicking the project icon in the project explorer, and selecting "Refresh Libs"

##Basic Usage Example

This example creates a simple form that, when pressed, draws a red oval the width and height of the screen.   Notice the general pattern where you use the pisces.Graphics class to perform your drawing, then use its getImage() method to obtain a Codename One Image of the graphics.  This image can be displayed in a number of ways, but this example sets it as the icon of a label since that is (probably) the simplest way.  

~~~

	public void startPisces() {
       
        if(current != null){
            current.show();
            return;                
        }
        Image img  = drawCircle(Display.getInstance().getDisplayWidth(), Display.getInstance().getDisplayHeight());
        final Label l = new Label(img);
        Form hi = new Form("Hi World"){



            @Override
            public void pointerPressed(int x, int y) {
                Image img = drawCircle(x, y);
                l.setIcon(img);

            }

        };
        hi.addComponent(new Label("Hi World"));
        hi.addComponent(l);
        hi.show();
        
    }
    
    Image drawCircle(int width, int height){
        Image out = null;
        pisces.Graphics g = new pisces.Graphics(width,height);
        try {
            
            g.setColor(pisces.Color.Red);
            g.setAntialiasing(true);

            g.drawOval(0, 0, width, height);

            out = g.toImage();
        } finally {
            g.dispose();
        }
        return out;
    } 
~~~

##Advanced Usage Example

The following example is from the original Pisces GAE port repository.  There are minor differences between this example and one that will work in Codename One.  For example, you would probably want to use the Graphics.toImage() method to obtain an image than the Graphics.toPNG() method (which is used in this example) because there is no need to waste cycles compressing the image when you are just displaying it to the screen.

[Example Source Code](https://code.google.com/p/cpi/source/browse/trunk/server/src/cpi/ProfileImage.java)

##Other Examples

[Tests Directory for Pisces Graphics (Not CN1 Port)](https://code.google.com/p/pisces-graphics/source/browse/trunk/test/src/)

##Fonts

Pisces has a flexible API that allows you to easily add your own fonts.  Check out the [CN1FontBox](https://github.com/shannah/CN1FontBox) project for TTF font support.

##JavaDocs

[Index Page](https://rawgithub.com/shannah/CN1Pisces/master/dist/javadoc/index.html)

###Key Classes

1. [Graphics](https://rawgithub.com/shannah/CN1Pisces/master/dist/javadoc/pisces/Graphics.html)
2. [Path](https://rawgithub.com/shannah/CN1Pisces/master/dist/javadoc/pisces/Path.html)
3. [Surface](https://rawgithub.com/shannah/CN1Pisces/master/dist/javadoc/pisces/Path.html)

##Todo

Codename One will be including a 2D drawing API in a future version (scheduled in the first part of 2014) so I'm not sure it is worth putting much more work into this library.  Some nice features that could probably be added without a tremendous amout of work:

1. Better integration with the Codename One class library.  E.g. Possibly creating a wrapper class that extends the Codename One Graphics class but includes support for more 2D drawing primitives.
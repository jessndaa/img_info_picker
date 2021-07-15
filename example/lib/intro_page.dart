import 'package:flutter/material.dart';

import 'gallery/gallery_page.dart';
import 'images/images_page.dart';

class IntroPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            ElevatedButton(
              child: Text('Gallery'),
              onPressed: () {
                Navigator.of(context)
                    .push(MaterialPageRoute(builder: (_) => GalleryPage()));
              },
            ),
            ElevatedButton(
              child: Text('Images'),
              onPressed: () {
                Navigator.of(context)
                    .push(MaterialPageRoute(builder: (_) => ImagesPage()));
              },
            ),
          ],
        ),
      ),
    );
  }
}

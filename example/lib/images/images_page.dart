import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:img_info_picker/img_info_picker.dart';
import 'package:img_info_picker/phone_photo.dart';

class ImagesPage extends StatefulWidget {
  @override
  _ImagesPageState createState() => _ImagesPageState();
}

class _ImagesPageState extends State<ImagesPage> {
  List<dynamic> images = [];
  final customImagePicker = ImageInfoPicker();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => getImages());
  }

  Future<void> getImages() async {
    await customImagePicker.getAllImages(callback: (msg) {
      print('the message is $msg');
      setState(() {
        images = msg;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Custom image picker plugin'),
      ),
      body: images.isNotEmpty
          ? GridView.builder(
              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                childAspectRatio: 4 / 5,
              ),
              itemCount: images.length,
              itemBuilder: (context, index) {
                var c = PhonePhoto.fromMap(jsonDecode(images[index]));
                return Center(
                  child: Container(
                    child: Image.file(
                      File(
                        c.uri,
                      ),
                    ),
                  ),
                );
              },
            )
          : Center(
              // child: CircularProgressIndicator(),
            ),
    );
  }
}

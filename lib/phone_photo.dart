import 'package:img_info_picker/utils/date_time.util.dart';

import 'model/file_type.enum.dart';

class PhonePhoto {
  final String id;
  final String albumName;
  final String uri;
  final DateTime? lastModifiedDate;
  final DateTime? createdDate;
  final FileType? fileType;
  PhonePhoto(this.id, this.albumName, this.uri, this.lastModifiedDate, this.createdDate, this.fileType);

  PhonePhoto.fromMap(Map<String, dynamic> map):
    id = map["Id"],
    albumName = map["AlbumName"],
    uri = map["PhotoUri"],
    lastModifiedDate = DateTimeUtility.getDateFromStringMillisencode(map["LastModifiedDate"]),
    createdDate = DateTimeUtility.getDateFromStringMillisencode(map["CreatedDate"]),
    fileType = FileType.values[int.tryParse(map["FileType"]) ?? (FileType.values.length -1)];

  @override
  String toString() {
    return 'PhonePhoto{id: $id, albumName: $albumName, photoUri: $uri, lastModifiedDate: $lastModifiedDate, createdDate: $createdDate, fileType: $fileType}';
  }
}

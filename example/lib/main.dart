import 'package:flutter/material.dart';
import 'package:social_sharing_plus/social_sharing_plus.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      home: SharePage(),
    );
  }
}

class SharePage extends StatefulWidget {
  const SharePage({super.key});

  @override
  State<SharePage> createState() => _SharePageState();
}

class _SharePageState extends State<SharePage> {
  final TextEditingController _controller = TextEditingController(text: 'https://g-website.server855.com/post/15046');
  static const List<SocialPlatform> _platforms = SocialPlatform.values;

  String? _mediaPath;
  List<String> _mediaPaths = [];

  Future<void> _share(
    SocialPlatform platform, {
    bool isMultipleShare = false,
  }) async {
    final String content = _controller.text;

    print('Text: $content');
    isMultipleShare
        ? await SocialSharingPlus.shareToSocialMediaWithMultipleMedia(
            platform,
            media: _mediaPaths,
            content: content,
            isOpenBrowser: true,
            onAppNotInstalled: () {
              ScaffoldMessenger.of(context)
                ..hideCurrentSnackBar()
                ..showSnackBar(SnackBar(
                  content:
                      Text('${platform.name.capitalize} is not installed.'),
                ));
            },
          )
        : await SocialSharingPlus.shareToSocialMedia(
            platform,
            content,
            media: _mediaPath,
            isOpenBrowser: true,
            onAppNotInstalled: () {
              ScaffoldMessenger.of(context)
                ..hideCurrentSnackBar()
                ..showSnackBar(SnackBar(
                  content:
                      Text('${platform.name.capitalize} is not installed.'),
                ));
            },
          );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('social_sharing_plus'),
      ),
      body: SingleChildScrollView(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.all(24),
                child: TextField(
                  controller: _controller,
                  decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    hintText: 'Enter a text',
                  ),
                ),
              ),
              ..._platforms.map(
                (SocialPlatform platform) => ElevatedButton(
                  onPressed: () => _share(
                    platform,
                    isMultipleShare: false,
                  ),
                  child: Text('Share to ${platform.name.capitalize}'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

extension StringExtension on String {
  String get capitalize => "${this[0].toUpperCase()}${substring(1)}";
}

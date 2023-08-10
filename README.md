<p align="center">
  <img src="https://github.com/jsericksk/Image-Loader-Legacy/raw/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="80">
</p>
<h1 align="center">
  Image Loader
</h2>

<p align="center">
  <img src="screenshots/demo-00.gif" width="320" height="691" />
  <img src="screenshots/demo-01.gif" width="320" height="691" />
  <img src="screenshots/demo-02.gif" width="320" height="691" />
</p>

This is an old project that I created when I was still programming only on my cell phone, using the [AIDE](https://play.google.com/store/apps/details?id=com.aide.ui) application. It was developed in August 2019 and maintained until 04/2021 when I stopped updating. I'm making the project public just as a reminder and to keep it on GitHub. At the time, I didn't use Git.

Currently the app on the Play Store contains the same code you are seeing here. If it ever gets updated again (certainly with Kotlin and Jetpack Compose), I'll update this description.

## Download

You can download the [app on Play Store](https://play.google.com/store/apps/details?id=com.kproject.imageloader).

## Clone repository

Unfortunately, if you just clone that project, it won't compile. There are several compatibility bugs and outdated libraries. I even tried to fix the issues to make it at least compilable, but I ended up giving up. You can give it a try if you wish, the code is free to be used for whatever you like (if you can call that mess of code).

## :pencil2: Functionalities

- Image search by keyword, with web scraping on Google;
- Extraction of all images from a page, from a URL;
- Download images;
- Download image links in .json or .txt format;
- Bookmark system of preferred pages.

## :monocle_face: Curiosities

- The app still works reasonably well, even though it's completely outdated even for the time it was developed.
- Apparently Google's web scraping algorithm still works, depending on the User Agent selected in the app's settings.
- A [Fragment](https://github.com/jsericksk/Image-Loader-Legacy/blob/main/app/src/main/java/com/kproject/imageloader/fragments/LoadPageTaskFragment.java) being used as a repository, with **AscyncTask**? Dark times. :)
- If you asked me today how I developed this using an outdated [LG L20](https://www.lg.com/au/smartphones/lg-L20-D105F-BLACK), I really wouldn't know how to answer. The me of today certainly couldn't do that, so I thank the me of the past for not giving up.
- I confess that it wasn't on purpose, but exactly today (08/10/23) it's been 4 years since I started this project. That's really weird, I hadn't planned on that.

## :hammer_and_wrench: Libraries used

- [Picasso](https://github.com/square/picasso): Image loading.
- [Jsoup](https://jsoup.org/): HTML parser.
- [TouchImageView](https://github.com/MikeOrtiz/TouchImageView): Zoom support.

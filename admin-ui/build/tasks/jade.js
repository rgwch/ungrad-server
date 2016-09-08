var gulp=require('gulp')
var paths=require('../paths')
var pug=require('gulp-pug')

gulp.task('jade',function(){
  gulp.src(paths.jade)
    .pipe(pug())
    .pipe(gulp.dest(paths.output))
})

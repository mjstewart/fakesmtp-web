const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');

module.exports = {
    entry: {
        app: ['./src/index.js']
    },
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname + '/dist')
    },
    module: {
        rules: [
            {
                test: /\.elm$/,
                exclude: [/elm-stuff/, /node_modules/],
                use: {
                    loader: 'elm-webpack-loader',
                }
            },
            {
                test: /\.css$/,
                use: ExtractTextPlugin.extract({
                    fallback: 'style-loader',
                    use: 'css-loader',
                })
            },
            {
                test: /\.html$/,
                use: 'html-loader',
                exclude: /node_modules/,
            },
            {
                test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
                use: {
                    loader: 'url-loader',
                    options: {
                        limit: 10000,
                        outputPath: 'img/',
                        publicPath: 'img/',
                        name: 'img-[hash:6].[name].[ext]',
                    },
                },
            }
        ],
        noParse: [/.elm$/]
    },
    plugins: [
        new CleanWebpackPlugin([path.resolve(__dirname, 'dist')], {
            root: '/',
            verbose: true,
          }),
        new ExtractTextPlugin("styles.css"),
        new HtmlWebpackPlugin({
            template: path.resolve(__dirname, 'src', 'index.html'),
            inject: 'body',
          }),
    ],
    devServer: {
        port: 9000
    },
};

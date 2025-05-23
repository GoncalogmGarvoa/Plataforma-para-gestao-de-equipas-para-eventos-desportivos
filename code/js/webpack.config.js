//const ESLintPlugin = require('eslint-webpack-plugin');
function delay(ms) {
    return new Promise((resolve, reject) => {
        setTimeout(resolve, ms);
    });
}
module.exports = {
    entry: './src/index.tsx',
    mode: 'development',
    devServer: {
        port: 8000,
        historyApiFallback: true,
        compress: false,
        proxy: [
            {
                context: ['/arbnet'],
                target: 'http://localhost:8080',
                // introducing an API delay to make testing easier
                pathRewrite: async function (path, req) {
                    return path;
                },
                onProxyRes: (proxyRes, req, res) => {
                    proxyRes.on('close', () => {
                        if (!res.writableEnded) {
                            res.destroy();
                        }
                    });
                    res.on('close', () => {
                        console.log("req close");
                        proxyRes.destroy();
                    });
                },
            },
        ],
    },
    resolve: {
        extensions: ['.js', '.ts', '.tsx'],
    },
    // plugins: [
    //     new ESLintPlugin({
    //         extensions: ['js', 'jsx', 'ts', 'tsx'],
    //     }),
    // ],
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader'],
            },
        ],
    },
};

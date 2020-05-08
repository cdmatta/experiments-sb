new Vue({
    el: "#app",
    data: {
        searchString: "",
        selectedPackage: null,
        dpkg: null,
        packages: null,
        packagesDisplayList: null
    },
    mounted() {
        axios
            .get('/all')
            .then(response => {
                this.dpkg = response.data;
                this.packages = _.values(this.dpkg);
                this.packagesListToShow = _.values(this.dpkg);
                this.updateDisplayPackages(this.searchString);
                return;
            })
    },
    watch: {
        searchString: function (val) {
            this.updateDisplayPackages(val);
        }
    },
    methods: {
        updateDisplayPackages(search) {
            this.packagesDisplayList = _.sortBy(_.filter(this.packages, function (p) { return p.name.includes(search) }), [function (p) { return p.name; }]);
        },
        updatePackageSelection(name) {
            this.selectedPackage = this.dpkg[name];
        }
    }
});